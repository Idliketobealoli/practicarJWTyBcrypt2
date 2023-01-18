import com.toxicbakery.bcrypt.Bcrypt

fun main(args: Array<String>) {
    println("Hello World!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")

    // PARA CIFRAR COSAS EN BCRYPT
    val originalPassword = "qwerty"
    val hash = Bcrypt.hash(originalPassword, 12)

    if (Bcrypt.verify("otra password", hash)) {
        println("Verified.")
    } else {
        println("This password does not match.")
    }
}