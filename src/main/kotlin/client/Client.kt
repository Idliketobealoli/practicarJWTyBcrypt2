package client

import common.Persona
import common.Request
import common.Response
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import kotlin.system.exitProcess

private const val PORT = 1707
private const val SERVER = "localhost"
private var token: String? = null
private lateinit var clientFactory: SSLSocketFactory
private lateinit var socket: SSLSocket

private val json = Json { ignoreUnknownKeys = true }

private lateinit var input: DataInputStream
private lateinit var output: DataOutputStream

fun main() {
    val file = System.getProperty("user.dir") +
            File.separator + "cert" +
            File.separator + "clientPracticar_keystore.jks"

    if (!Files.exists(Path.of(file))) {
        System.err.println("Cert file not found.")
        exitProcess(0)
    }

    val propFile = System.getProperty("user.dir") +
            File.separator + "src" +
            File.separator + "main" +
            File.separator + "resources" +
            File.separator + "client.properties"

    if (!Files.exists(Path.of(propFile))) {
        System.err.println("Properties file not found.")
        exitProcess(0)
    }

    val properties = Properties()
    properties.load(FileInputStream(propFile))
    System.setProperty("javax.net.debug", properties.getProperty("debug"))
    System.setProperty("javax.net.ssl.trustStore", file)
    System.setProperty("javax.net.ssl.trustStorePassword", properties.getProperty("password"))

    clientFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
    socket = clientFactory.createSocket(SERVER, PORT) as SSLSocket

    println("Client connected to $SERVER:$PORT")

    input = DataInputStream(socket.inputStream)
    output = DataOutputStream(socket.outputStream)

    val user = Persona("Juan")

    while (true) {
        if(token == null) {
            token = solicitarToken(user)
            println(token)
        } else {
            doThings(user)
        }
    }
}

fun doThings(user: Persona) {
    var userInput = 0
    while (userInput < 1 || userInput > 3) {
        println("""
        Select something to do: 
        
        1. Receive hello message
        2. Exit
        """.trimIndent())

        userInput = readln().toIntOrNull() ?: 0
    }
    when (userInput) {
        1 -> {
            val request = Request(token, user, Request.Type.HOLA)
            val jsonRequest = json.encodeToString(request)
            output.writeUTF(jsonRequest)
            println(input.readUTF())
        }
        2 -> {
            val request = Request(token, user, Request.Type.EXIT)
            val jsonRequest = json.encodeToString(request)
            output.writeUTF(jsonRequest)
            println(input.readUTF())

            exitProcess(0)
        }
    }
}

fun solicitarToken(user: Persona): String? {
    val request = Request<Persona>(null, user, Request.Type.TOKEN)
    val jsonRequest = json.encodeToString(request)
    output.writeUTF(jsonRequest)
    val response = input.readUTF()
    return json.decodeFromString<Response<String>>(response).content
}
