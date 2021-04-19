package search

import java.io.FileNotFoundException

fun main(args: Array<String>) {
    if (args.size == 2 && args[0] == "--data") {
        val searchEngine = try {
            SearchEngine(args[1])
        } catch (e: FileNotFoundException) {
            println("Incorrect data source")
            return
        }
        searchEngine.startEngine()
    } else {
        println("Incorrect commandline parameters")
        return
    }
}


