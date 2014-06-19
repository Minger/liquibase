package liquibase.change.core;

import java.io.IOException;
import java.io.InputStream;

import liquibase.change.*;
import liquibase.changelog.ChangeLogParameters;
import liquibase.exception.SetupException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

/**
 * Represents a Change for custom SQL stored in a File.
 * <p/>
 * To create an instance call the constructor as normal and then call
 *
 * @author <a href="mailto:csuml@yahoo.co.uk">Paul Keeble</a>
 * @link{#setResourceAccesssor(ResourceAccessor)} before calling setPath otherwise the
 * file will likely not be found.
 */
@DatabaseChange(name = "sqlFile",
        description = "The 'sqlFile' tag allows you to specify any sql statements and have it stored external in a file. It is useful for complex changes that are not supported through LiquiBase's automated refactoring tags such as stored procedures.\n" +
                "\n" +
                "The sqlFile refactoring finds the file by searching in the following order:\n" +
                "\n" +
                "The file is searched for in the classpath. This can be manually set and by default the liquibase startup script adds the current directory when run.\n" +
                "The file is searched for using the file attribute as a file name. This allows absolute paths to be used or relative paths to the working directory to be used.\n" +
                "The 'sqlFile' tag can also support multiline statements in the same file. Statements can either be split using a ; at the end of the last line of the SQL or a go on its own on the line between the statements can be used.Multiline SQL statements are also supported and only a ; or go statement will finish a statement, a new line is not enough. Files containing a single statement do not need to use a ; or go.\n" +
                "\n" +
                "The sql file can also contain comments of either of the following formats:\n" +
                "\n" +
                "A multiline comment that starts with /* and ends with */.\n" +
                "A single line comment starting with <space>--<space> and finishing at the end of the line",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
public class SQLFileChange extends AbstractSQLChange {

    private String path;
    private Boolean relativeToChangelogFile;

    @Override
    public boolean generateStatementsVolatile(ExecutionOptions options) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsVolatile(ExecutionOptions options) {
        return false;
    }

    @DatabaseChangeProperty(description = "The file path of the SQL file to load", requiredForDatabase = "all", exampleValue = "my/path/file.sql")
    public String getPath() {
        return path;
    }

    /**
     * Sets the file name but setUp must be called for the change to have impact.
     *
     * @param fileName The file to use
     */
    public void setPath(String fileName) {
        path = fileName;
    }

    /**
     * The encoding of the file containing SQL statements
     *
     * @return the encoding
     */
    @DatabaseChangeProperty(exampleValue = "utf8")
    public String getEncoding() {
        return encoding;
    }

    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }


    public Boolean isRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    public void setRelativeToChangelogFile(Boolean relativeToChangelogFile) {
        this.relativeToChangelogFile = relativeToChangelogFile;
    }

    @Override
    public void finishInitialization() throws SetupException {
        if (path == null) {
            throw new SetupException("<sqlfile> - No path specified");
        }
    }

    public InputStream openSqlStream() throws IOException {
        if (path == null) {
            return null;
        }

        InputStream inputStream = null;
        try {
            inputStream = StreamUtil.openStream(path, isRelativeToChangelogFile(), getChangeSet(), getResourceAccessor());
        } catch (IOException e) {
            throw new IOException("Unable to read file '" + path + "'", e);
        }
        if (inputStream == null) {
            throw new IOException("Unable to read file '" + path + "'");
        }
        return inputStream;
    }

    @Override
    public ValidationErrors validate(ExecutionOptions options) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (StringUtils.trimToNull(getPath()) == null) {
            validationErrors.addError("'path' is required");
        }
        return validationErrors;
    }

    @Override
    public String getConfirmationMessage() {
        return "SQL in file " + path + " executed";
    }

    @Override
    @DatabaseChangeProperty(isChangeProperty = false)
    public String getSql() {
        String sql = super.getSql();
        if (sql == null) {
            InputStream sqlStream;
            try {
                sqlStream = openSqlStream();
                if (sqlStream == null) {
                    return null;
                }
                String content = StreamUtil.getStreamContents(sqlStream, encoding);
                if (getChangeSet() != null) {
                    ChangeLogParameters parameters = getChangeSet().getChangeLogParameters();
                    if (parameters != null) {
                        content = parameters.expandExpressions(content);
                    }
                }
                return content;
            } catch (IOException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        } else {
            return sql;
        }
    }

    @Override
    public void setSql(String sql) {
        if (getChangeSet() != null && getChangeSet().getChangeLogParameters() != null) {
            sql = getChangeSet().getChangeLogParameters().expandExpressions(sql);
        }
        super.setSql(sql);
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
