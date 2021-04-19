package search

import java.io.File
import java.lang.IllegalArgumentException

class SearchEngine(dataSource: String) {
    private val data: List<String> = getData(dataSource)

    enum class Strategy {
        ALL,
        ANY,
        NONE,
        NULL
    }

    companion object {
        fun getItemsInvertedIndex(items: List<String>): MutableMap<String, MutableSet<Int>> {
            val invertedIndex = mutableMapOf<String, MutableSet<Int>>()
            items.forEachIndexed { index, string ->
                string.split(" ").forEach {
                    invertedIndex[it.toLowerCase()]?.add(index) ?: invertedIndex.put(
                        it.toLowerCase(),
                        mutableSetOf(index)
                    )
                }
            }
            return invertedIndex
        }

        fun getItemsBySearchQuery(
            items: List<String>,
            itemsInvertedIndex: MutableMap<String, MutableSet<Int>>,
            searchStrategy: Strategy,
            query: List<String>
        ): List<String> {
            val validItemsIndexes = getValidItemsIndexes(searchStrategy, query, itemsInvertedIndex, items.size)
            val validItems = mutableListOf<String>()
            validItemsIndexes.forEach { validItems.add(items[it]) }
            return validItems
        }

        private fun getValidItemsIndexes(
            searchStrategy: Strategy,
            query: List<String>,
            itemsInvertedIndex: MutableMap<String, MutableSet<Int>>,
            itemsCount: Int
        ): Set<Int> {
            var itemsIndexes = (0 until itemsCount).toMutableSet()
            return when (searchStrategy) {
                Strategy.ALL -> {
                    query.forEach {
                        itemsIndexes = itemsIndexes.intersect(itemsInvertedIndex[it] ?: emptySet()).toMutableSet()
                    }
                    itemsIndexes
                }
                Strategy.ANY -> itemsIndexes.apply {
                    clear()
                    query.forEach {
                        this += union(itemsInvertedIndex[it] ?: emptySet()).toMutableSet()
                    }
                }
                Strategy.NONE ->
                    itemsIndexes.apply {
                        query.forEach { this -= itemsInvertedIndex[it] ?: emptySet() }
                    }
                else -> emptySet()
            }
        }
    }

    object UserInterface {
        private var menu = arrayOf("1. Find item", "2. Print all items", "0. Exit")

        fun showMenu() {
            println("=== Menu ===")
            menu.forEach { menuPoint -> println(menuPoint) }
        }

        fun getUserChoice(): Int {
            var userChoice = -1
            while (true) {
                try {
                    userChoice = readLine()!!.toInt()
                } catch (e: NumberFormatException) {
                    continue
                }
                if (userChoice in 0..menu.lastIndex) {
                    break;
                }
                println("Incorrect option! Try again.")
            }
            return userChoice
        }

        fun getUserQuery(): List<String> {
            println("Enter title or author name to search books:")
            return readLine()!!.toLowerCase().split(" ").filter { it != "" }.map { it.toLowerCase() }
        }

        fun getUsersSearchStrategy() =
            run {
                println("Select a matching strategy: ALL, ANY, NONE")
                try {
                    Strategy.valueOf(readLine()!!.toUpperCase())
                } catch (e: IllegalArgumentException) {
                    Strategy.NULL
                }
            }

        fun printItems(items: List<String>, search: Boolean = false) {
            if (items.isEmpty()) {
                println("No matching books found")
                return
            }
            if (!search) {
                println("=== List of books ===")
            }
            items.forEach { item -> println(item) }
        }
    }

    private fun getData(dataSource: String) = File(dataSource).readLines()

    fun startEngine() {
        while (true) {
            UserInterface.showMenu()
            when (UserInterface.getUserChoice()) {
                1 -> UserInterface.printItems(
                    getItemsBySearchQuery(
                        data,
                        getItemsInvertedIndex(data),
                        UserInterface.getUsersSearchStrategy(),
                        UserInterface.getUserQuery()
                    ),
                    true
                )
                2 -> UserInterface.printItems(data)
                0 -> {
                    println("Bye!")
                    break
                }
            }
        }
    }
}