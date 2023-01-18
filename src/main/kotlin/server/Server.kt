package server

import common.Persona
import common.Request
import common.Response
import jwt.create
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLServerSocketFactory
import kotlin.concurrent.thread
import kotlin.system.exitProcess

private const val PORT = 1707

private val json = Json { ignoreUnknownKeys = true }

private lateinit var input: DataInputStream
private lateinit var output: DataOutputStream

fun main() {
    var numClients = 0

    val file = System.getProperty("user.dir") +
            File.separator + "cert" +
            File.separator + "serverPracticar_keystore.jks"

    if (!Files.exists(Path.of(file))) {
        System.err.println("Cert file for this server not found.")
        exitProcess(0)
    }

    val propFile = System.getProperty("user.dir") +
            File.separator + "src" +
            File.separator + "main" +
            File.separator + "resources" +
            File.separator + "server.properties"

    if (!Files.exists(Path.of(propFile))) {
        System.err.println("Properties file for this server not found.")
        exitProcess(0)
    }

    val properties = Properties()
    properties.load(FileInputStream(propFile))
    System.setProperty("javax.net.debug", properties.getProperty("debug"))
    System.setProperty("javax.net.ssl.keyStore", file)
    System.setProperty("javax.net.ssl.keyStorePassword", properties.getProperty("password"))

    val serverFactory = SSLServerSocketFactory.getDefault() as SSLServerSocketFactory
    val serverSocket = serverFactory.createServerSocket(PORT) as SSLServerSocket

    println("Server started.")

    while (true) {
        val socket = serverSocket.accept()
        numClients++
        println("Attending client $numClients : ${socket.remoteSocketAddress}")

        thread {
            processClient(socket, numClients)
        }
    }
}

fun processClient(socket: Socket, numClients: Int) {
    input = DataInputStream(socket.inputStream)
    output = DataOutputStream(socket.outputStream)
    var tokenExpired = true

    println("Attending client $numClients : ${socket.remoteSocketAddress}")

    while (!socket.isClosed) {
        val jsonRequest = input.readUTF()
        val request = json.decodeFromString<Request<Persona>>(jsonRequest)

        if (request.type != Request.Type.TOKEN) {
            tokenExpired = checkToken(request)
        }

        if (tokenExpired && request.type != Request.Type.TOKEN) {
            val response = Response("Token expired", Response.Type.ERROR)
            output.writeUTF(json.encodeToString(response))

            output.close()
            input.close()
            socket.close()
        }

        when(request.type) {
            Request.Type.HOLA -> { sendHola(request.content) }
            Request.Type.TOKEN -> { sendToken(request.content) }
            Request.Type.EXIT -> { sendExit(socket) }
        }
    }
}

fun sendExit(socket: Socket) {
    val response = Response("Closing connection", Response.Type.OK)
    output.writeUTF(json.encodeToString(response))
    output.close()
    input.close()
    socket.close()
}

fun sendToken(content: Any?) {
    if (content is Persona) {
        val response = Response(create(content), Response.Type.OK)
        output.writeUTF(json.encodeToString(response))
    } else {
        val response = Response("Unable to identify user.", Response.Type.ERROR)
        output.writeUTF(json.encodeToString(response))
    }
}

fun sendHola(content: Any?) {
    if (content is Persona) {
        val response = Response("Hola ${content.name}!", Response.Type.OK)
        output.writeUTF(json.encodeToString(response))
    }
}

fun checkToken(req: Request<out Any>): Boolean {
    val token = req.token?.let { jwt.decode(it) } ?: return true

    if (token.getClaim("nombre").asString().isNotBlank()) {
        println("el token tiene el nombre ${token.getClaim("nombre").asString()}, todo bien.")
    } else return true

    return !token.expiresAt.after(Date(System.currentTimeMillis()))
}
