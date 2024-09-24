package com.draft.restapi.common.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.model.naming.ImplicitUniqueKeyNameSource;

import com.draft.restapi.common.enums.ConstraintPattern;

public class CustomImplicitNamingStrategy extends ImplicitNamingStrategyJpaCompliantImpl {

    @Override
    public Identifier determineUniqueKeyName(ImplicitUniqueKeyNameSource source) {
        String tableName = source.getTableName().getText();
        String columnName = source.getColumnNames().get(0).getText();
        String constraintName = String.format(ConstraintPattern.UNIQUE_KEY.getPattern(), tableName, columnName);
        return Identifier.toIdentifier("\"" + constraintName + "\""); // to preserve case sensitivity and avoid quoting issues
    }

}