package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.executor.ExecutionOptions;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.structure.core.Column;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Adds functionality for setting the sequence to be owned by the column with the default value
 */
public class AddDefaultValueGeneratorPostgres extends AddDefaultValueGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddDefaultValueStatement statement, ExecutionOptions options) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        return database instanceof PostgresDatabase;
    }

    @Override
    public Sql[] generateSql(final AddDefaultValueStatement statement, final ExecutionOptions options,
                             final SqlGeneratorChain sqlGeneratorChain) {

        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        if (!(statement.getDefaultValue() instanceof SequenceNextValueFunction)) {
            return super.generateSql(statement, options, sqlGeneratorChain);
        }

        List<Sql> commands = new ArrayList<Sql>(Arrays.asList(super.generateSql(statement, options, sqlGeneratorChain)));
        // for postgres, we need to also set the sequence to be owned by this table for true serial like functionality.
        // this will allow a drop table cascade to remove the sequence as well.
        SequenceNextValueFunction sequenceFunction = (SequenceNextValueFunction) statement.getDefaultValue();

        Sql alterSequenceOwner = new UnparsedSql("ALTER SEQUENCE " + database.escapeSequenceName(statement.getCatalogName(),
                statement.getSchemaName(), sequenceFunction.getValue()) + " OWNED BY " +
                database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + "."
                + database.escapeObjectName(statement.getColumnName(), Column.class));
        commands.add(alterSequenceOwner);
        return commands.toArray(new Sql[commands.size()]);
    }
}
