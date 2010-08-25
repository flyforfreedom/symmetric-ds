/*
 * SymmetricDS is an open source database synchronization solution.
 *   
 * Copyright (C) Eric Long <erilong@users.sourceforge.net>,
 *               Eric Weimer <eweimer@users.sourceforge.net>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see
 * <http://www.gnu.org/licenses/>.
 */

package org.jumpmind.symmetric.db.db2;

import org.apache.commons.lang.StringUtils;
import org.jumpmind.symmetric.db.AbstractDbDialect;
import org.jumpmind.symmetric.db.BinaryEncoding;
import org.jumpmind.symmetric.db.IDbDialect;
import org.jumpmind.symmetric.model.Trigger;

public class Db2DbDialect extends AbstractDbDialect implements IDbDialect {

    @Override
    protected void initTablesAndFunctionsForSpecificDialect() {
    }
    
    protected boolean createTablesIfNecessary() {
        boolean tablesCreated = super.createTablesIfNecessary();
        if (tablesCreated) {
             long triggerHistId = jdbcTemplate.queryForLong("select max(trigger_hist_id) from " + tablePrefix + "_trigger_hist")+1;
             jdbcTemplate.update("alter table " + tablePrefix + "_trigger_hist alter column trigger_hist_id restart with " + triggerHistId);
             log.info("DB2ResettingAutoIncrementColumns", tablePrefix + "_trigger_hist");
             long outgoingBatchId = jdbcTemplate.queryForLong("select max(batch_id) from " + tablePrefix + "_outgoing_batch")+1;
             jdbcTemplate.update("alter table " + tablePrefix + "_outgoing_batch alter column batch_id restart with " + outgoingBatchId);
             log.info("DB2ResettingAutoIncrementColumns", tablePrefix + "_outgoing_batch");
             long dataId = jdbcTemplate.queryForLong("select max(data_id) from " + tablePrefix + "_data")+1;
             jdbcTemplate.update("alter table " + tablePrefix + "_data alter column data_id restart with " + dataId);
             log.info("DB2ResettingAutoIncrementColumns", tablePrefix + "_data");
        }
        return tablesCreated;
    }

    @Override
    protected boolean doesTriggerExistOnPlatform(String catalog, String schema, String tableName, String triggerName) {
        schema = schema == null ? (getDefaultSchema() == null ? null : getDefaultSchema()) : schema;
        return jdbcTemplate.queryForInt("select count(*) from syscat.triggers where trigname = ? and trigschema = ?",
                new Object[] { triggerName.toUpperCase(), schema.toUpperCase() }) > 0;
    }

    @Override
    public boolean isBlobSyncSupported() {
        return true;
    }

    @Override
    public boolean isClobSyncSupported() {
        return true;
    }

    @Override
    public BinaryEncoding getBinaryEncoding() {
        return BinaryEncoding.HEX;
    }

    public void disableSyncTriggers(String nodeId) {
    }

    public void enableSyncTriggers() {
    }

    public String getSyncTriggersExpression() {
        return "1=1";
    }
    
    @Override
    public String getTransactionTriggerExpression(String defaultCatalog, String defaultSchema, Trigger trigger) {
        return "null";
    }

    @Override
    public String getSelectLastInsertIdSql(String sequenceName) {
        return "values IDENTITY_VAL_LOCAL()";
    }

    public boolean isNonBlankCharColumnSpacePadded() {
        return true;
    }

    public boolean isCharColumnSpaceTrimmed() {
        return false;
    }

    public boolean isEmptyStringNulled() {
        return false;
    }

    @Override
    public boolean supportsTransactionId() {
        return false;
    }

    @Override
    public boolean storesUpperCaseNamesInCatalog() {
        return true;
    }

    @Override
    public boolean supportsGetGeneratedKeys() {
        return false;
    }

    @Override
    protected boolean allowsNullForIdentityColumn() {
        return false;
    }

    public void purge() {
    }

    public String getDefaultCatalog() {
        return null;
    }

    @Override
    public String getDefaultSchema() {
        if (StringUtils.isBlank(this.defaultSchema)) {
            this.defaultSchema = (String) jdbcTemplate.queryForObject("values CURRENT SCHEMA", String.class);
        }
        return this.defaultSchema;
    }

    @Override
    public String getIdentifierQuoteString() {
        return "";
    }

    @Override
    public void truncateTable(String tableName) {
        jdbcTemplate.update("delete from " + tableName);
    }
    
}
