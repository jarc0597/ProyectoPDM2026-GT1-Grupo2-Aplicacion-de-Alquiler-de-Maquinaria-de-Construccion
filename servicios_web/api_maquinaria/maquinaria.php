<?php
// maquinaria.php
// API REST para gestionar maquinaria desde MySQL.

require_once __DIR__ . "/conexion.php";

$metodo = $_SERVER["REQUEST_METHOD"];

switch ($metodo) {
    case "GET":
        if (isset($_GET["id"])) {
            obtenerMaquinariaPorId($pdo, (int) $_GET["id"]);
        } else {
            listarMaquinarias($pdo);
        }
        break;

    case "POST":
        insertarMaquinaria($pdo);
        break;

    case "PUT":
        if (!isset($_GET["id"])) {
            responderError("Debe enviar el ID de la maquinaria a actualizar", 400);
            break;
        }

        actualizarMaquinaria($pdo, (int) $_GET["id"]);
        break;

    case "DELETE":
        if (!isset($_GET["id"])) {
            responderError("Debe enviar el ID de la maquinaria a eliminar", 400);
            break;
        }

        eliminarMaquinaria($pdo, (int) $_GET["id"]);
        break;

    default:
        responderError("Método no permitido", 405);
        break;
}

function listarMaquinarias(PDO $pdo): void
{
    $sql = "
        SELECT 
            m.id_maquinaria,
            m.codigo_interno,
            m.nombre_equipo,
            m.marca,
            m.modelo,
            m.capacidad,
            m.descripcion,
            m.costo_hora,
            m.costo_dia,
            m.stock,
            m.imagen_url,
            m.id_categoria,
            c.nombre_categoria,
            m.id_estado,
            e.nombre_estado,
            m.activo
        FROM maquinaria m
        INNER JOIN categorias_maquinaria c 
            ON c.id_categoria = m.id_categoria
        INNER JOIN estados_maquinaria e 
            ON e.id_estado = m.id_estado
        WHERE m.activo = 1
        ORDER BY m.nombre_equipo ASC
    ";

    $stmt = $pdo->query($sql);
    $datos = $stmt->fetchAll(PDO::FETCH_ASSOC);

    responderOk("Maquinarias consultadas correctamente", $datos);
}

function obtenerMaquinariaPorId(PDO $pdo, int $idMaquinaria): void
{
    $sql = "
        SELECT 
            m.id_maquinaria,
            m.codigo_interno,
            m.nombre_equipo,
            m.marca,
            m.modelo,
            m.capacidad,
            m.descripcion,
            m.costo_hora,
            m.costo_dia,
            m.stock,
            m.imagen_url,
            m.id_categoria,
            c.nombre_categoria,
            m.id_estado,
            e.nombre_estado,
            m.activo
        FROM maquinaria m
        INNER JOIN categorias_maquinaria c 
            ON c.id_categoria = m.id_categoria
        INNER JOIN estados_maquinaria e 
            ON e.id_estado = m.id_estado
        WHERE m.id_maquinaria = ?
        LIMIT 1
    ";

    $stmt = $pdo->prepare($sql);
    $stmt->execute([$idMaquinaria]);
    $dato = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$dato) {
        responderError("No se encontró la maquinaria solicitada", 404);
        return;
    }

    responderOk("Maquinaria consultada correctamente", $dato);
}

function insertarMaquinaria(PDO $pdo): void
{
    $input = obtenerJsonEntrada();

    validarCamposMaquinaria($input);

    $sql = "
        INSERT INTO maquinaria
        (
            codigo_interno,
            nombre_equipo,
            marca,
            modelo,
            capacidad,
            descripcion,
            costo_hora,
            costo_dia,
            stock,
            imagen_url,
            id_categoria,
            id_estado,
            activo
        )
        VALUES
        (
            :codigo_interno,
            :nombre_equipo,
            :marca,
            :modelo,
            :capacidad,
            :descripcion,
            :costo_hora,
            :costo_dia,
            :stock,
            :imagen_url,
            :id_categoria,
            :id_estado,
            1
        )
    ";

    $stmt = $pdo->prepare($sql);

    $stmt->execute([
        ":codigo_interno" => $input["codigo_interno"],
        ":nombre_equipo" => $input["nombre_equipo"],
        ":marca" => $input["marca"],
        ":modelo" => $input["modelo"],
        ":capacidad" => $input["capacidad"] ?? "",
        ":descripcion" => $input["descripcion"],
        ":costo_hora" => (float) $input["costo_hora"],
        ":costo_dia" => (float) $input["costo_dia"],
        ":stock" => (int) $input["stock"],
        ":imagen_url" => $input["imagen_url"] ?? "",
        ":id_categoria" => (int) $input["id_categoria"],
        ":id_estado" => (int) $input["id_estado"]
    ]);

    responderOk("Maquinaria insertada correctamente", [
        "id_generado" => $pdo->lastInsertId()
    ], 201);
}

function actualizarMaquinaria(PDO $pdo, int $idMaquinaria): void
{
    $input = obtenerJsonEntrada();

    validarCamposMaquinaria($input);

    $sql = "
        UPDATE maquinaria
        SET
            codigo_interno = :codigo_interno,
            nombre_equipo = :nombre_equipo,
            marca = :marca,
            modelo = :modelo,
            capacidad = :capacidad,
            descripcion = :descripcion,
            costo_hora = :costo_hora,
            costo_dia = :costo_dia,
            stock = :stock,
            imagen_url = :imagen_url,
            id_categoria = :id_categoria,
            id_estado = :id_estado
        WHERE id_maquinaria = :id_maquinaria
    ";

    $stmt = $pdo->prepare($sql);

    $stmt->execute([
        ":codigo_interno" => $input["codigo_interno"],
        ":nombre_equipo" => $input["nombre_equipo"],
        ":marca" => $input["marca"],
        ":modelo" => $input["modelo"],
        ":capacidad" => $input["capacidad"] ?? "",
        ":descripcion" => $input["descripcion"],
        ":costo_hora" => (float) $input["costo_hora"],
        ":costo_dia" => (float) $input["costo_dia"],
        ":stock" => (int) $input["stock"],
        ":imagen_url" => $input["imagen_url"] ?? "",
        ":id_categoria" => (int) $input["id_categoria"],
        ":id_estado" => (int) $input["id_estado"],
        ":id_maquinaria" => $idMaquinaria
    ]);

    if ($stmt->rowCount() === 0) {
        responderError("No se encontró la maquinaria o no hubo cambios", 404);
        return;
    }

    responderOk("Maquinaria actualizada correctamente", [
        "id_maquinaria" => $idMaquinaria
    ]);
}

function eliminarMaquinaria(PDO $pdo, int $idMaquinaria): void
{
    // Eliminación lógica: no borra el registro, solo lo marca como inactivo.
    $sql = "
        UPDATE maquinaria
        SET activo = 0
        WHERE id_maquinaria = ?
    ";

    $stmt = $pdo->prepare($sql);
    $stmt->execute([$idMaquinaria]);

    if ($stmt->rowCount() === 0) {
        responderError("No se encontró la maquinaria a eliminar", 404);
        return;
    }

    responderOk("Maquinaria eliminada correctamente", [
        "id_maquinaria" => $idMaquinaria
    ]);
}

function obtenerJsonEntrada(): array
{
    $json = file_get_contents("php://input");
    $input = json_decode($json, true);

    if (!is_array($input)) {
        responderError("Debe enviar datos en formato JSON", 400);
        exit;
    }

    return $input;
}

function validarCamposMaquinaria(array $input): void
{
    $camposRequeridos = [
        "codigo_interno",
        "nombre_equipo",
        "marca",
        "modelo",
        "descripcion",
        "costo_hora",
        "costo_dia",
        "stock",
        "id_categoria",
        "id_estado"
    ];

    foreach ($camposRequeridos as $campo) {
        if (!isset($input[$campo]) || $input[$campo] === "") {
            responderError("El campo {$campo} es obligatorio", 400);
            exit;
        }
    }
}

function responderOk(string $mensaje, mixed $data = null, int $codigoHttp = 200): void
{
    http_response_code($codigoHttp);

    echo json_encode([
        "ok" => true,
        "mensaje" => $mensaje,
        "data" => $data
    ], JSON_UNESCAPED_UNICODE);
}

function responderError(string $mensaje, int $codigoHttp = 400): void
{
    http_response_code($codigoHttp);

    echo json_encode([
        "ok" => false,
        "mensaje" => $mensaje
    ], JSON_UNESCAPED_UNICODE);
}