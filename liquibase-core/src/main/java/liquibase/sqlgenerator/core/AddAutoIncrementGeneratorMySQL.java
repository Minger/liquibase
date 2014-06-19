package liquibase.sqlgenerator.core;

import liquibase.database.core.MySQLDatabase;
import liquibase.executor.ExecutionOptions;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddAutoIncrementStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

/**
 * SQLite does not support this ALTER TABLE operation until now.
 * For more information see: http://www.sqlite.org/omitted.html.
 * This is a small work around...
 */
public class AddAutoIncrementGeneratorMySQL extends AddAutoIncrementGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddAutoIncrementStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof MySQLDatabase;
    }

    @Override
    public Sql[] generateSql(final AddAutoIncrementStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {

    	Sql[] sql = super.generateSql(statement, options, sqlGeneratorChain);

    	if(statement.getStartWith() != null){
	    	MySQLDatabase mysqlDatabase = (MySQLDatabase) options.getRuntimeEnvironment().getTargetDatabase();
	        String alterTableSql = "ALTER TABLE "
	            + mysqlDatabase.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
	            + " "
	            + mysqlDatabase.getTableOptionAutoIncrementStartWithClause(statement.getStartWith());

	        sql = concact(sql, new UnparsedSql(alterTableSql));
    	}

        return sql;
    }

	private Sql[] concact(Sql[] origSql, UnparsedSql unparsedSql) {
		Sql[] changedSql = new Sql[origSql.length+1];
		System.arraycopy(origSql, 0, changedSql, 0, origSql.length);
		changedSql[origSql.length] = unparsedSql;

		return changedSql;
	}

	private DatabaseObject getAffectedTable(AddAutoIncrementStatement statement) {
		return new Table().setName(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName()));
	}
}