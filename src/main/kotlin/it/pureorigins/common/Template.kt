package it.pureorigins.common

import freemarker.core.CommonMarkupOutputFormat
import freemarker.core.CommonTemplateMarkupOutputModel
import freemarker.template.*
import freemarker.template.utility.DeepUnwrap
import kotlinx.serialization.serializer
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import net.minecraft.network.chat.TextComponent
import java.io.StringWriter
import java.io.Writer
import java.time.*
import java.util.*
import kotlin.random.Random


private fun String.template(args: Map<String, Any?>, configuration: Configuration.() -> Unit): String {
    val reader = reader()
    val writer = StringWriter()
    val configuration = Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).apply {
        registeredCustomOutputFormats = listOf(JsonOutputFormat)
        whitespaceStripping = false
        logTemplateExceptions = false
        objectWrapper = ObjectWrapper
        outputEncoding = "utf8"
        isAPIBuiltinEnabled = true
        setSharedVariable("json", JsonTemplateMethodModel)
        setSharedVariable("plain", PlainTemplateMethodModel)
        setSharedVariable("unicode", UnicodeTemplateMethodModel)
        setSharedVariable("random", Random)
        configuration()
    }
    val template = Template("Configuration", reader, configuration)
    template.process(args, writer)
    return writer.toString()
}

fun String.template(args: Map<String, Any?>, locale: Locale = Locale.ROOT) = template(args) {
    this.locale = locale
}

fun String.template(vararg args: Pair<String, Any?>, locale: Locale = Locale.ROOT) = template(args.toMap(), locale)

fun String.templateJson(args: Map<String, Any?>, locale: Locale = Locale.ROOT) = template(args) {
    this.locale = locale
    outputFormat = JsonOutputFormat
}

fun String.templateJson(vararg args: Pair<String, Any?>, locale: Locale = Locale.ROOT) = templateJson(args.toMap(), locale)

fun String.templateText(args: Map<String, Any?>, locale: Locale = Locale.ROOT): MutableText {
    val text = templateJson(args, locale)
    return if (text.startsWith('{') || text.startsWith('[')) {
        textFromJson(text)
    } else {
        TextComponent(text)
    }
}

fun String.templateText(vararg args: Pair<String, Any?>, locale: Locale = Locale.ROOT) = templateText(args.toMap(), locale)

private class TemplateJsonOutputModel(plainTextContent: String?, markupContent: String?) : CommonTemplateMarkupOutputModel<TemplateJsonOutputModel>(plainTextContent, markupContent) {
    override fun getOutputFormat() = JsonOutputFormat
}

private object JsonOutputFormat : CommonMarkupOutputFormat<TemplateJsonOutputModel>() {
    override fun getName() = "JSON"
    override fun getMimeType() = null
    override fun output(textToEsc: String, out: Writer) {
        textToEsc.forEach {
            when (it) {
                '\\', '"' -> out.write("\\$it")
                else -> out.write(it.code)
            }
        }
    }
    override fun escapePlainText(plainTextContent: String) = buildString {
        plainTextContent.forEach {
            when (it) {
                '\\', '"' -> append("\\$it")
                else -> append(it)
            }
        }
    }
    override fun isLegacyBuiltInBypassed(builtInName: String) = false
    override fun newTemplateMarkupOutputModel(plainTextContent: String?, markupContent: String?) = TemplateJsonOutputModel(plainTextContent, markupContent)
}

private object UnicodeTemplateMethodModel : TemplateMethodModelEx {
    override fun exec(args: MutableList<Any?>): Any {
        if (args.size != 1) throw TemplateModelException("Wrong arguments")
        return SimpleScalar((args[0] as SimpleNumber).asNumber.toChar().toString())
    }
}

private object JsonTemplateMethodModel : TemplateMethodModelEx {
    override fun exec(args: MutableList<Any?>): Any {
        if (args.size != 1) throw TemplateModelException("Wrong arguments")
        val arg: Any = DeepUnwrap.unwrap(args[0] as TemplateModel) ?: return SimpleScalar("null")
        return when (arg) {
            is BaseComponent -> SimpleScalar(ComponentSerializer.toString(arg))
            (arg is Array<*> && arg.all { it is BaseComponent }) -> @Suppress("UNCHECKED_CAST") SimpleScalar((arg as SpigotText).toJson())
            is PaperText -> SimpleScalar(arg.toJson())
            is Text -> SimpleScalar(arg.toJson())
            else -> SimpleScalar(json.encodeToString(json.serializersModule.serializer(arg.javaClass), arg))
        }
    }
}

private object PlainTemplateMethodModel : TemplateMethodModelEx {
    override fun exec(args: MutableList<Any?>): Any {
        if (args.size != 1) throw TemplateModelException("Wrong arguments")
        val arg: Any = DeepUnwrap.unwrap(args[0] as TemplateModel) ?: return SimpleScalar("null")
        return when (arg) {
            is BaseComponent -> SimpleScalar(net.md_5.bungee.api.chat.TextComponent.toPlainText(arg))
            (arg is Array<*> && arg.all { it is BaseComponent }) -> @Suppress("UNCHECKED_CAST") SimpleScalar((arg as SpigotText).toPlainText())
            is PaperText -> SimpleScalar(arg.toPlainText())
            is Text -> SimpleScalar(arg.toPlainText())
            else -> SimpleScalar(arg.toString())
        }
    }
}

private object ObjectWrapper : DefaultObjectWrapper(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS) {
    override fun wrap(obj: Any?): TemplateModel? = when (obj) {
        is Int -> SimpleNumber(obj)
        is Long -> SimpleNumber(obj)
        is Byte -> SimpleNumber(obj)
        is Short -> SimpleNumber(obj)
        is Double -> SimpleNumber(obj)
        is Float -> SimpleNumber(obj)
        is LocalDateTime -> SimpleDate(Date.from(obj.toInstant(ZoneOffset.UTC)), TemplateDateModel.DATETIME)
        is LocalDate -> SimpleDate(Date.from(obj.atStartOfDay().toInstant(ZoneOffset.UTC)), TemplateDateModel.DATE)
        is LocalTime -> SimpleDate(Date.from(obj.atDate(LocalDate.EPOCH).toInstant(ZoneOffset.UTC)), TemplateDateModel.TIME)
        is Instant -> SimpleDate(Date.from(obj), TemplateDateModel.DATETIME)
        is ZonedDateTime -> SimpleDate(Date.from(obj.toInstant()), TemplateDateModel.DATETIME)
        is OffsetDateTime -> SimpleDate(Date.from(obj.toInstant()), TemplateDateModel.DATETIME)
        is OffsetTime -> SimpleDate(Date.from(obj.atDate(LocalDate.EPOCH).toInstant()), TemplateDateModel.TIME)
        is LinkedHashMap<*, *> -> DefaultMapAdapter.adapt(obj, this)
        else -> super.handleUnknownType(obj)
    }
}
