<?php
// conexion.php
// Archivo central para conectar la API con MySQL.

header("Content-Type: application/json; charset=UTF-8");

// Permite pruebas desde Android, navegador o herramientas externas.
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization");

// Responde rápido a solicitudes OPTIONS.
if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") {
    http_response_code(200);
    exit;
}

$host = "localhost";
$dbname = "alquiler_maquinaria_pdm";
$user = "root";
$password = "";

// Si tu MySQL tiene contraseña, colócala en $password.

try {
    $pdo = new PDO(
        "mysql:host=$host;dbname=$dbname;charset=utf8mb4",
        $user,
        $password
    );

    // Muestra errores de base de datos durante desarrollo.
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

} catch (PDOException $e) {
    http_response_code(500);

    echo json_encode([
        "ok" => false,
        "mensaje" => "Error de conexión a la base de datos",
        "error" => $e->getMessage()
    ], JSON_UNESCAPED_UNICODE);

    exit;
}