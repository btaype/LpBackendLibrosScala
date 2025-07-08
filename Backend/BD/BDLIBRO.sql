
CREATE TABLE persona (
    id_persona  SERIAL PRIMARY  KEY,
    
    nombre VARCHAR(100) NOT  NULL,
    rol VARCHAR(20) NOT NULL  CHECK (rol IN ('cliente', 'admin')),
    contrasena VARCHAR(100)  NOT NULL,
    correo VARCHAR(100) UNIQUE  NOT NULL,

    dni VARCHAR(20) UNIQUE NOT  NULL
);


CREATE TABLE categoria (
    id_categoria SERIAL   PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL
);


CREATE TABLE libro (
    id_libro SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT  NULL,
     precio NUMERIC(10, 2) NOT NULL,

    id_categoria INTEGER NOT  NULL  REFERENCES categoria(id_categoria) ON DELETE CASCADE,
    nombrepdf VARCHAR(255),
     nombreimagen VARCHAR(255)
);


CREATE TABLE venta (
        id_venta SERIAL PRIMARY KEY,
    id_persona  INTEGER REFERENCES persona(id_persona) ON  DELETE SET NULL,

        id_libro INTEGER REFERENCES libro(id_libro) ON DELETE SET NULL,
    fecha  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE tarjeta (
    id_tarjeta  SERIAL PRIMARY KEY,

    numero  VARCHAR(30) NOT NULL UNIQUE,

        ccv_numero  VARCHAR(4) NOT NULL,
    saldo  NUMERIC(20, 2) NOT NULL 
);



CREATE OR REPLACE FUNCTION insertar_libro(
    p_nombre VARCHAR,
    p_precio NUMERIC, p_id_categoria INTEGER,
    p_nombrepdf VARCHAR, p_nombreimagen VARCHAR
)
RETURNS VOID AS $$
BEGIN
    INSERT INTO libro (nombre, precio, id_categoria, nombrepdf, nombreimagen)
    VALUES (p_nombre, p_precio, p_id_categoria, p_nombrepdf, p_nombreimagen);
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION insertar_persona(
    p_nombre VARCHAR,
    p_rol VARCHAR,
    p_contrasena VARCHAR,
    p_correo VARCHAR,
    p_dni VARCHAR
) RETURNS VARCHAR
LANGUAGE plpgsql
AS $$
DECLARE
    resultado VARCHAR;
BEGIN
    BEGIN
        
        IF EXISTS (SELECT 1 FROM persona WHERE dni = p_dni) THEN  

            RETURN 'ERROR: Ya existe una persona con ese DNI';

        END IF;

        
        IF EXISTS (SELECT 1 FROM persona WHERE correo = p_correo) THEN
            RETURN 'ERROR: Ya existe una persona con ese correo';
        END IF;

       
        INSERT INTO persona (nombre, rol, contrasena, correo, dni)  
        
        VALUES (p_nombre, p_rol, p_contrasena, p_correo, p_dni);

        RETURN 'OK: Persona insertada correctamente';

    EXCEPTION
        WHEN others THEN
            RETURN 'ERROR: ' || SQLERRM;
    END;
END;
$$;

CREATE OR REPLACE FUNCTION obtener_categorias()
RETURNS TABLE(id_categoria INTEGER, nombre VARCHAR)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT c.id_categoria, c.nombre
    FROM categoria c
    ORDER BY c.id_categoria;
END;
$$;

CREATE OR REPLACE FUNCTION buscar_persona_por_correo(p_correo VARCHAR)
RETURNS TABLE(id_persona INTEGER, nombre VARCHAR, rol VARCHAR, contrasena VARCHAR, correo VARCHAR, dni VARCHAR) AS $$
BEGIN
    RETURN QUERY
    SELECT p.id_persona, p.nombre, p.rol, p.contrasena, p.correo, p.dni
    FROM persona p
    WHERE p.correo = p_correo;
END;
$$ LANGUAGE plpgsql;




CREATE OR REPLACE FUNCTION eliminar_libro(idlibros INTEGER)
RETURNS BOOLEAN AS $$
DECLARE

    eliminado BOOLEAN := FALSE;
BEGIN
    DELETE FROM libro WHERE id_libro = idlibros;

    IF FOUND THEN
        eliminado := TRUE;
    END IF;



    RETURN eliminado;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION reportedevntas(mes INT, anio INT) 
RETURNS TABLE (
    id_libro INT,
    nombre_libro VARCHAR,
	
    categoria VARCHAR,
    dni VARCHAR,
     nombre_persona VARCHAR,
    correo VARCHAR,
	
    precio NUMERIC,
    total_ventas NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        l.id_libro,
        COALESCE(l.nombre, 'LIBRO ELIMINADO') AS nombre_libro,
		
        COALESCE(c.nombre, 'CATEGORÍA DESCONOCIDA') AS categoria,
        p.dni,
        p.nombre AS nombre_persona,
        p.correo,
		
        COALESCE(l.precio, 0) AS precio,
		
        SUM(COALESCE(l.precio, 0)) OVER () AS total_ventas
    FROM venta v
    LEFT JOIN libro l ON v.id_libro = l.id_libro
	
    LEFT JOIN categoria c ON l.id_categoria = c.id_categoria
     INNER JOIN persona p ON v.id_persona = p.id_persona
    WHERE EXTRACT(MONTH FROM v.fecha) = mes
	
      AND EXTRACT(YEAR FROM v.fecha) = anio
    ORDER BY v.fecha;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION pagina_principal()
RETURNS TABLE (
    id_libro INT,
    nombre VARCHAR,precio NUMERIC,
    id_categoria INT,
    nombre_categoria VARCHAR,
    nombrepdf VARCHAR,
    nombreimagen VARCHAR
) AS $$
BEGIN
    RETURN QUERY
	
    SELECT 
        l.id_libro,
        l.nombre,
        l.precio,
        l.id_categoria,
		
        c.nombre AS nombre_categoria,
        l.nombrepdf,
        l.nombreimagen
		
    FROM libro l
	
    JOIN categoria c ON l.id_categoria = c.id_categoria
    ORDER BY l.id_libro;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION comprar_libro(
    idlibro INT,idpersona INT, numtarjeta VARCHAR, ccvtxt VARCHAR
) 
RETURNS VARCHAR AS $$
DECLARE
    precio NUMERIC;
    saldo_actual NUMERIC;
    idt INT;
BEGIN
    


    IF NOT EXISTS (SELECT 1 FROM persona WHERE id_persona = idpersona) THEN
        RETURN 'ERROR: la persona no existe';
    END IF;

   
    IF NOT EXISTS (SELECT 1 FROM libro WHERE id_libro = idlibro) THEN
        RETURN 'ERROR: el libro no existe';
    END IF;

   
    SELECT precio INTO precio FROM libro WHERE id_libro = idlibro;

    
    SELECT id_tarjeta, saldo INTO idt, saldo_actual
    FROM tarjeta
    WHERE numero = numtarjeta AND ccv_numero = ccvtxt;

    IF NOT FOUND THEN
       RETURN 'ERROR: tarjeta  no valida';
    END IF;

    IF saldo_actual < precio THEN
        RETURN 'ERROR: no alcanzaste  el saldo';
    END IF;

    
    UPDATE tarjeta SET saldo = saldo - precio WHERE id_tarjeta = idt;

    
    INSERT INTO venta(id_persona, id_libro) VALUES (idpersona, idlibro);

     RETURN 'OK: compra realizada';
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION obtener_correo(idpersona INT)
RETURNS VARCHAR AS $$
DECLARE
    correo_resultado VARCHAR;
BEGIN
    SELECT correo  INTO correo_resultado
    FROM persona

    WHERE id_persona = idpersona;

    RETURN correo_resultado;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION obtener_nombrepdf(idlibro INT)
RETURNS VARCHAR AS $$
DECLARE

    pdf_resultado VARCHAR;
BEGIN
    SELECT  nombrepdf INTO pdf_resultado
    FROM libro

    WHERE id_libro = idlibro;

    RETURN  pdf_resultado;

END;
$$ LANGUAGE plpgsql;



---hgh
CREATE OR REPLACE FUNCTION obtener_librosfiltro(
    p_id_categoria INT,
        p_precio_min NUMERIC,
    p_precio_max NUMERIC, p_orden BOOLEAN
)
RETURNS TABLE (
     id_libro INT,
    nombre VARCHAR,
    precio NUMERIC,
    id_categoria INT,
     nombre_categoria VARCHAR,
    nombrepdf VARCHAR,
        nombreimagen VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        l.id_libro,l.nombre,l.precio,
        l.id_categoria,
        c.nombre AS nombre_categoria,
        l.nombrepdf,
        l.nombreimagen

    FROM libro l
    JOIN categoria c ON l.id_categoria = c.id_categoria
    WHERE l.id_categoria = p_id_categoria
      AND l.precio BETWEEN p_precio_min AND p_precio_max

    ORDER BY 
        CASE 
            WHEN p_orden THEN l.nombre
            ELSE NULL
        END ASC,

        CASE 
            WHEN NOT p_orden THEN l.nombre
            ELSE NULL
        END DESC;
END;
$$ LANGUAGE plpgsql;