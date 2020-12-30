package com.github.ryarnyah.querydsl

import com.google.common.base.Strings
import com.mysema.codegen.model.SimpleType
import com.querydsl.codegen.BeanSerializer
import com.querydsl.codegen.Property
import com.querydsl.codegen.Serializer
import com.querydsl.sql.Configuration
import com.querydsl.sql.SQLTemplates
import com.querydsl.sql.codegen.DefaultNamingStrategy
import com.querydsl.sql.codegen.MetaDataExporter
import com.querydsl.sql.codegen.NamingStrategy
import com.querydsl.sql.codegen.support.NumericMapping
import com.querydsl.sql.codegen.support.RenameMapping
import com.querydsl.sql.codegen.support.TypeMapping
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.impldep.org.apache.maven.plugin.MojoExecutionException
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.sql.DriverManager
import java.util.regex.Pattern

abstract class AbstractMetadataExportTask : DefaultTask() {

    /**
     * JDBC driver class name
     * @parameter required=true
     */
    @Input
    var jdbcDriver: String? = null

    /**
     * JDBC connection url
     * @parameter required=true
     */
    @Input
    var jdbcUrl: String? = null

    /**
     * JDBC connection username
     * @parameter
     */
    @Input
    var jdbcUser: String? = null

    /**
     * JDBC connection password
     * @parameter
     */
    @Input
    var jdbcPassword: String? = null

    /**
     * name prefix for querydsl-types (default: "Q")
     * @parameter default-value="Q"
     */
    @Internal
    var namePrefix: String = "Q"

    /**
     * name suffix for querydsl-types (default: "")
     * @parameter default-value=""
     */
    @Internal
    var nameSuffix: String = ""

    /**
     * name prefix for bean types (default: "")
     * @parameter default-value=""
     */
    @Internal
    var beanPrefix: String = ""

    /**
     * name suffix for bean types (default: "")
     * @parameter default-value=""
     */
    @Internal
    var beanSuffix: String = ""

    /**
     * package name for sources
     * @parameter
     * @required
     */
    @Input
    var packageName: String? = null

    /**
     * package name for bean sources (default: packageName)
     * @parameter
     */
    @Internal
    var beanPackageName: String? = packageName

    /**
     * schemaPattern a schema name pattern; must match the schema name
     * as it is stored in the database; "" retrieves those without a schema;
     * `null` means that the schema name should not be used to narrow
     * the search (default: null)
     *
     * @parameter
     */
    @Internal
    var schemaPattern: String? = null

    /**
     * a catalog name; must match the catalog name as it
     * is stored in the database; "" retrieves those without a catalog;
     * `null` means that the catalog name should not be used to narrow
     * the search
     */
    @Internal
    var catalogPattern: String? = null

    /**
     * tableNamePattern a table name pattern; must match the
     * table name as it is stored in the database (default: null)
     *
     * @parameter
     */
    @Internal
    var tableNamePattern: String? = null

    /**
     * namingstrategy class to override (default: DefaultNamingStrategy)
     *
     * @parameter
     */
    @Internal
    var namingStrategyClass: String? = null

    /**
     * name for bean serializer class
     *
     * @parameter
     */
    @Internal
    var beanSerializerClass: String? = null

    /**
     * name for serializer class
     *
     * @parameter
     */
    @Internal
    var serializerClass: String? = null

    /**
     * serialize beans as well
     *
     * @parameter default-value=false
     */
    @Internal
    var exportBeans = false

    /**
     * additional interfaces to be implemented by beans
     *
     * @parameter
     */
    @Internal
    var beanInterfaces: List<String> = listOf()

    /**
     * switch for `toString` addition
     *
     * @parameter default-value=false
     */
    @Internal
    var beanAddToString = false

    /**
     * switch for full constructor addition
     *
     * @parameter default-value=false
     */
    @Internal
    var beanAddFullConstructor = false

    /**
     * switch to print supertype content
     *
     * @parameter default-value=false
     */
    @Internal
    var beanPrintSupertype = false

    /**
     * wrap key properties into inner classes (default: false)
     *
     * @parameter default-value=false
     */
    @Internal
    var innerClassesForKeys = false

    /**
     * export validation annotations (default: false)
     *
     * @parameter default-value=false
     */
    @Internal
    var validationAnnotations = false

    /**
     * export column annotations (default: false)
     *
     * @parameter default-value=false
     */
    @Internal
    var columnAnnotations = false

    /**
     * custom type classnames to use
     *
     * @parameter
     */
    @Internal
    var customTypes: List<String> = listOf()

    /**
     * custom type mappings to use
     *
     * @parameter
     */
    @Internal
    var typeMappings: List<TypeMapping> = listOf()

    /**
     * custom numeric mappings
     *
     * @parameter
     */
    @Internal
    var numericMappings: List<NumericMapping> = listOf()

    /**
     * custom rename mappings
     *
     * @parameter
     */
    @Internal
    var renameMappings: List<RenameMapping> = listOf()

    /**
     * switch for generating scala sources
     *
     * @parameter default-value=false
     */
    @Internal
    var createScalaSources = false

    /**
     * switch for using schema as suffix in package generation, full package name will be
     * `${packageName}.${schema}`
     *
     * @parameter default-value=false
     */
    @Internal
    var schemaToPackage = false

    /**
     * switch to normalize schema, table and column names to lowercase
     *
     * @parameter default-value=false
     */
    @Internal
    var lowerCase = false

    /**
     * switch to export tables
     *
     * @parameter default-value=true
     */
    @Internal
    var exportTables = false

    /**
     * switch to export views
     *
     * @parameter default-value=true
     */
    @Internal
    var exportViews = false

    /**
     * switch to export all types
     *
     * @parameter default-value=false
     */
    @Internal
    var exportAll = false

    /**
     * switch to export primary keys
     *
     * @parameter default-value=true
     */
    @Internal
    var exportPrimaryKeys = false

    /**
     * switch to export foreign keys
     *
     * @parameter default-value=true
     */
    @Internal
    var exportForeignKeys = false

    /**
     * switch to export direct foreign keys
     *
     * @parameter default-value=true
     */
    @Internal
    var exportDirectForeignKeys = false

    /**
     * switch to export inverse foreign keys
     *
     * @parameter default-value=true
     */
    @Internal
    var exportInverseForeignKeys = false

    /**
     * override default column order (default: alphabetical)
     *
     * @parameter
     */
    @Internal
    var columnComparatorClass: String? = null

    /**
     * switch to enable spatial type support
     *
     * @parameter default-value=false
     */
    @Internal
    var spatial = false

    /**
     * Comma-separated list of table types to export (allowable values will
     * depend on JDBC driver). Allows for arbitrary set of types to be exported,
     * e.g.: "TABLE, MATERIALIZED VIEW". The exportTables and exportViews
     * parameters will be ignored if this parameter is set. (default: none)
     *
     * @parameter
     */
    @Internal
    var tableTypesToExport: String? = null

    /**
     * java import added to generated query classes:
     * com.bar for package (without .* notation)
     * com.bar.Foo for class
     *
     * @parameter
     */
    @Internal
    var imports: List<String> = listOf()

    /**
     * target source folder to create the sources into (e.g. target/generated-sources/java)
     *
     * @parameter default-value=${project.buildDir.absolutePath}/generated/source/querydsl/main/java"
     * @required
     */
    @OutputDirectory
    var targetFolder: File = File("${project.buildDir.absolutePath}/generated/source/querydsl/main/java")

    /**
     * target source folder to create the bean sources into.
     *
     * @parameter default-value=targetFolder
     */
    @OutputDirectory
    var beanTargetFolder: File = targetFolder

    /**
     * source file encoding.
     *
     * @parameter default-value=UTF-8
     */
    @Internal
    var sourceEncoding: String? = null

    @TaskAction
    fun export() {
        val configuration = Configuration(SQLTemplates.DEFAULT)
        val namingStrategy = if (namingStrategyClass != null) {
            Class.forName(namingStrategyClass).getDeclaredConstructor().newInstance() as NamingStrategy
        } else {
            DefaultNamingStrategy()
        }

        // defaults for Scala
        if (createScalaSources) {
            if (serializerClass == null) {
                serializerClass = "com.querydsl.scala.sql.ScalaMetaDataSerializer"
            }
            if (exportBeans && beanSerializerClass == null) {
                beanSerializerClass = "com.querydsl.scala.ScalaBeanSerializer"
            }
        }

        val exporter = MetaDataExporter()
        exporter.setNamePrefix(emptyIfSetToBlank(namePrefix))
        exporter.setNameSuffix(Strings.nullToEmpty(nameSuffix))
        exporter.setBeanPrefix(Strings.nullToEmpty(beanPrefix))
        exporter.setBeanSuffix(Strings.nullToEmpty(beanSuffix))
        exporter.setBeansTargetFolder(beanTargetFolder)
        exporter.setCreateScalaSources(createScalaSources)
        exporter.setPackageName(packageName)
        exporter.setBeanPackageName(beanPackageName)
        exporter.setInnerClassesForKeys(innerClassesForKeys)
        exporter.setTargetFolder(targetFolder)
        exporter.setNamingStrategy(namingStrategy)
        exporter.setCatalogPattern(catalogPattern)
        exporter.setSchemaPattern(processBlankValues(schemaPattern))
        exporter.setTableNamePattern(tableNamePattern)
        exporter.setColumnAnnotations(columnAnnotations)
        exporter.setValidationAnnotations(validationAnnotations)
        @Suppress("DEPRECATION")
        exporter.setSchemaToPackage(schemaToPackage)
        exporter.setLowerCase(lowerCase)
        exporter.setExportTables(exportTables)
        exporter.setExportViews(exportViews)
        exporter.setExportAll(exportAll)
        exporter.setTableTypesToExport(tableTypesToExport)
        exporter.setExportPrimaryKeys(exportPrimaryKeys)
        exporter.setExportForeignKeys(exportForeignKeys)
        exporter.setExportDirectForeignKeys(exportDirectForeignKeys)
        exporter.setExportInverseForeignKeys(exportInverseForeignKeys)
        exporter.setSpatial(spatial)

        if (imports.isNotEmpty()) {
            exporter.setImports(imports.toTypedArray())
        }

        if (serializerClass != null) {
            try {
                exporter.setSerializerClass(Class.forName(serializerClass) as Class<out Serializer>?)
            } catch (e: ClassNotFoundException) {
                throw MojoExecutionException(e.message, e)
            }
        }
        if (exportBeans) {
            if (beanSerializerClass != null) {
                exporter.setBeanSerializerClass(Class.forName(beanSerializerClass) as Class<out Serializer>?)
            } else {
                val serializer = BeanSerializer()
                for (iface in beanInterfaces) {
                    val sepIndex = iface.lastIndexOf('.')
                    if (sepIndex < 0) {
                        serializer.addInterface(SimpleType(iface))
                    } else {
                        val packageName = iface.substring(0, sepIndex)
                        val simpleName = iface.substring(sepIndex + 1)
                        serializer.addInterface(SimpleType(iface, packageName, simpleName))
                    }
                }
                serializer.setAddFullConstructor(beanAddFullConstructor)
                serializer.setAddToString(beanAddToString)
                serializer.setPrintSupertype(beanPrintSupertype)
                exporter.setBeanSerializer(serializer)
            }
        }
        exporter.setSourceEncoding(Charset.forName(sourceEncoding ?: StandardCharsets.UTF_8.name()).name())

        for (cl in customTypes) {
            configuration.register(Class.forName(cl).getDeclaredConstructor().newInstance() as com.querydsl.sql.types.Type<*>?)
        }
        for (mapping in typeMappings) {
            mapping.apply(configuration)
        }
        for (mapping in numericMappings) {
            mapping.apply(configuration)
        }
        for (mapping in renameMappings) {
            mapping.apply(configuration)
        }

        if (columnComparatorClass != null) {
            try {
                exporter.setColumnComparatorClass(
                    Class.forName(columnComparatorClass)
                        .asSubclass(Comparator::class.java) as Class<out java.util.Comparator<Property>>?
                )
            } catch (e: ClassNotFoundException) {
                throw MojoExecutionException(e.message, e)
            }
        }

        exporter.setConfiguration(configuration)

        Class.forName(jdbcDriver)
        DriverManager.getConnection(
            jdbcUrl,
            jdbcUser,
            jdbcPassword
        ).use {
            exporter.export(it.metaData)
        }
    }

    private fun emptyIfSetToBlank(value: String?): String? {
        val setToBlank = value == null || value.equals("BLANK", ignoreCase = true)
        return if (setToBlank) "" else value
    }

    private fun processBlankValues(value: String?): String? {
        return if (value == null) {
            null
        } else BLANK_VALUE_PATTERN.matcher(value).replaceAll(BLANK_VALUE_REPLACEMENT)
    }

    companion object {
        private val BLANK_VALUE_PATTERN: Pattern = Pattern.compile("(^|,)BLANK(,|$)", Pattern.CASE_INSENSITIVE)
        private const val BLANK_VALUE_REPLACEMENT = "$1$2"
    }
}