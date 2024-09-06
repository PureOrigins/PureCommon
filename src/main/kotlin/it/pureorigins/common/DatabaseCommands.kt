package it.pureorigins.common

import com.mojang.brigadier.arguments.StringArgumentType.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseCommand(private val plugin: PureCommon, private val config: Config) {
    fun register() = with(plugin) {
        registerCommand(database)
        registerCommand(query)
    }

    // /database test url <url>
    // /database test username <username>
    // /database test password <password>

    private val database get() = literal("database") {
        requiresPermission("purecommon.database")
        success { source.sendNullableMessage(config.database.usage?.templateText()) }
        then(argument("name", string()) {
            suggestions { getDatabases().keys }
            success {
                val name = getString(this, "name")
                val db = getDatabases()[name] ?: return@success source.sendNullableMessage(config.invalidDatabase?.templateText("name" to name))
                val database = Database.connect(db.url, user = db.username, password = db.password)
                source.sendNullableMessage(config.database.get?.templateText("name" to name, "url" to db.url, "database" to database))
            }
            then(argument("url", string()) {
                success {
                    val name = getString(this, "name")
                    val url = getString(this, "url")
                    try {
                        val db = Database.connect(url)
                        val databases = getDatabases()
                        databases[name] = DatabaseInfo(url)
                        setDatabases(databases)
                        source.sendNullableMessage(config.database.set?.templateText("name" to name, "url" to url, "database" to db))
                    } catch (e: Exception) {
                        source.sendNullableMessage(config.error?.templateText("name" to name, "url" to url, "error" to e))
                    }
                }
                then(argument("username", string()) {
                    success {
                        val name = getString(this, "name")
                        val url = getString(this, "url")
                        val username = getString(this, "username")
                        try {
                            val db = Database.connect(url, user = username)
                            val databases = getDatabases()
                            databases[name] = DatabaseInfo(url, username)
                            setDatabases(databases)
                            source.sendNullableMessage(config.database.set?.templateText("name" to name, "url" to url, "database" to db))
                        } catch (e: Exception) {
                            source.sendNullableMessage(config.error?.templateText("name" to name, "url" to url, "error" to e))
                        }
                    }
                    then(argument("password", string()) {
                        success {
                            val name = getString(this, "name")
                            val url = getString(this, "url")
                            val username = getString(this, "username")
                            val password = getString(this, "password")
                            try {
                                val db = Database.connect(url, user = username, password = password)
                                val databases = getDatabases()
                                databases[name] = DatabaseInfo(url, username, password)
                                setDatabases(databases)
                                source.sendNullableMessage(config.database.set?.templateText("name" to name, "url" to url, "database" to db))
                            } catch (e: Exception) {
                                source.sendNullableMessage(config.error?.templateText("name" to name, "url" to url, "error" to e))
                            }
                        }
                    })
                })
            })
        })
    }

    private val query get() = literal("query") {
        requiresPermission("purecommon.query")
        success { source.sendNullableMessage(config.query.usage?.templateText()) }
        then(argument("database", string()) {
            suggestions { getDatabases().keys }
            success { source.sendNullableMessage(config.query.usage?.templateText()) }
            then(argument("query", greedyString()) {
                success {
                    val databaseName = getString(this, "database")
                    val query = getString(this, "query")
                    val database = getDatabases()[databaseName] ?: return@success source.sendNullableMessage(config.invalidDatabase?.templateText("name" to databaseName))
                    try {
                        transaction(Database.connect(database.url, user = database.username, password = database.password)) {
                            val result = exec(query) {
                                val columns = it.metaData.columnCount
                                
                                val list = mutableListOf<Array<String?>>()
                                list += Array(columns) { i -> it.metaData.getColumnName(i + 1) }
                                
                                var size = 0
                                while (size < config.query.maxResultSize && it.next()) {
                                    list += Array(columns) { i -> it.getString(i + 1) }
                                    size++
                                }
                                while (it.next()) size++
                                source.sendNullableMessage(config.query.result?.templateText("query" to query, "url" to database.url, "results" to list, "size" to size))
                            }
                            if (result == null) {
                                source.sendNullableMessage(config.query.result?.templateText("query" to query, "url" to database.url, "results" to emptyList<Array<String>>(), "size" to 0))
                            }
                        }
                    } catch (e: Exception) {
                        source.sendNullableMessage(config.error?.templateText("name" to name, "url" to database.url, "error" to e))
                    }
                }
            })
        })
    }
    
    private fun getDatabases(): MutableMap<String, DatabaseInfo> {
        return json.readFileAs(plugin.file("databases.json"), mutableMapOf())
    }
    
    private fun setDatabases(databases: Map<String, DatabaseInfo>) {
        json.writeFile(plugin.file("databases.json"), databases)
    }
    
    @Serializable
    data class DatabaseInfo(val url: String, val username: String = "", val password: String = "")
    
    @Serializable
    data class Config(
        val invalidDatabase: String? = "<dark_gray>Unknown database: \${name}\"</dark_gray>",
        val error: String? = "<dark_gray>An error has occurred: \${error.message}\"</dark_gray>",
        val database: Database = Database(),
        val query: Query = Query()
    ) {
        @Serializable
        data class Database(
            val usage: String? = "<dark_gray>Usage:</dark_gray> <gray>/database \\<name\\> [url]</gray>",
            val get: String? = "<gray>Database \${name} (\${database.vendor}) is '\${url}'</gray>",
            val set: String? = "<gray>Database \${database.vendor} saved as '\${name}'</gray>",
        )
        
        @Serializable
        data class Query(
            val usage: String? = "<dark_gray>Usage:</dark_gray> <gray>/query \\<database\\> \\<query\\></gray>",
            val result: String? = "<gray>Query executed, \${size} result<#if size != 1>s</#if><#if (size > 0)>\n<#list results as row><#list row as column>\${column}<#sep>, </#list><#sep>\n</#list><#if (size > results?size - 1)>\n...</#if></#if></gray>",
            val maxResultSize: Int = 10
        )
    }
}