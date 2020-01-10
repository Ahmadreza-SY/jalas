package ir.ac.ut.jalas.utils


object TemplateEngine {
    private val cachedTemplates = mutableMapOf<String, String>()
    private fun templateOf(name: String): String {
        return cachedTemplates[name] ?: {
            val loaded = javaClass.getResource("/template/$name").readText()
            cachedTemplates[name] = loaded
            loaded
        }()
    }

    fun render(name: String, map: Map<String, String>): String {
        var template = templateOf(name)
        map.forEach { (key, value) ->
            template = template.replace("{{$key}}", value, false)
        }
        return template
    }
}