-- Crear tabla persona
CREATE TABLE persona (
    id_persona SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('cliente', 'admin')),
    contrasena VARCHAR(100) NOT NULL,
    correo VARCHAR(100) UNIQUE NOT NULL,
    dni VARCHAR(20) UNIQUE NOT NULL
);

-- Crear tabla categoria
CREATE TABLE categoria (
    id_categoria SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL
);

-- Crear tabla libro
CREATE TABLE libro (
    id_libro SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    precio NUMERIC(10, 2) NOT NULL,
    id_categoria INTEGER NOT NULL REFERENCES categoria(id_categoria) ON DELETE CASCADE,
    nombrepdf VARCHAR(255),
    nombreimagen VARCHAR(255)
);

-- Crear tabla venta con ON DELETE CASCADE para limpiar ventas al borrar persona/libro
CREATE TABLE venta (
    id_venta SERIAL PRIMARY KEY,
    id_persona INTEGER NOT NULL REFERENCES persona(id_persona) ON DELETE CASCADE,
    id_libro INTEGER NOT NULL REFERENCES libro(id_libro) ON DELETE CASCADE,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE OR REPLACE FUNCTION insertar_libro(
    p_nombre VARCHAR,
    p_precio NUMERIC,
    p_id_categoria INTEGER,
    p_nombrepdf VARCHAR,
    p_nombreimagen VARCHAR
)
RETURNS VOID AS $$
BEGIN
    INSERT INTO libro (nombre, precio, id_categoria, nombrepdf, nombreimagen)
    VALUES (p_nombre, p_precio, p_id_categoria, p_nombrepdf, p_nombreimagen);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insertar_libro(
    p_nombre VARCHAR,
    p_precio NUMERIC,
    p_id_categoria INTEGER,
    p_nombrepdf VARCHAR,
    p_nombreimagen VARCHAR
)
RETURNS VOID AS $$
BEGIN
    INSERT INTO libro (nombre, precio, id_categoria, nombrepdf, nombreimagen)
    VALUES (p_nombre, p_precio, p_id_categoria, p_nombrepdf, p_nombreimagen);
EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Error al insertar libro: %', SQLERRM;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION buscar_persona_por_correo(p_correo VARCHAR)
RETURNS TABLE(id_persona INTEGER, nombre VARCHAR, rol VARCHAR, contrasena VARCHAR, correo VARCHAR, dni VARCHAR) AS $$
BEGIN
    RETURN QUERY
    SELECT p.id_persona, p.nombre, p.rol, p.contrasena, p.correo, p.dni
    FROM persona p
    WHERE p.correo = p_correo;
END;
$$ LANGUAGE plpgsql;