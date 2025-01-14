/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.iotdb.confignode.service.executor;

import org.apache.iotdb.confignode.consensus.response.DataNodesInfoDataSet;
import org.apache.iotdb.confignode.consensus.response.StorageGroupSchemaDataSet;
import org.apache.iotdb.confignode.exception.physical.UnknownPhysicalPlanTypeException;
import org.apache.iotdb.confignode.partition.DataNodeInfo;
import org.apache.iotdb.confignode.partition.PartitionTable;
import org.apache.iotdb.confignode.partition.StorageGroupSchema;
import org.apache.iotdb.confignode.physical.PhysicalPlan;
import org.apache.iotdb.confignode.physical.sys.QueryDataNodeInfoPlan;
import org.apache.iotdb.confignode.physical.sys.RegisterDataNodePlan;
import org.apache.iotdb.confignode.physical.sys.SetStorageGroupPlan;
import org.apache.iotdb.consensus.common.DataSet;
import org.apache.iotdb.service.rpc.thrift.TSStatus;

import java.util.List;
import java.util.Map;

public class PlanExecutor {

  private final PartitionTable partitionTable = new PartitionTable();

  public PlanExecutor() {
    // empty constructor
  }

  public DataSet executorQueryPlan(PhysicalPlan plan) throws UnknownPhysicalPlanTypeException {
    switch (plan.getType()) {
      case QueryDataNodeInfo:
        return queryDataNodesInfo((QueryDataNodeInfoPlan) plan);
      case QueryStorageGroupSchema:
        return queryStorageGroupSchema();
      default:
        throw new UnknownPhysicalPlanTypeException(plan.getType());
    }
  }

  public TSStatus executorNonQueryPlan(PhysicalPlan plan) throws UnknownPhysicalPlanTypeException {
    switch (plan.getType()) {
      case RegisterDataNode:
        return partitionTable.registerDataNode((RegisterDataNodePlan) plan);
      case SetStorageGroup:
        return partitionTable.setStorageGroup((SetStorageGroupPlan) plan);
      default:
        throw new UnknownPhysicalPlanTypeException(plan.getType());
    }
  }

  private DataNodesInfoDataSet queryDataNodesInfo(QueryDataNodeInfoPlan plan) {
    Map<Integer, DataNodeInfo> infoMap = partitionTable.getDataNodeInfo(plan);
    DataNodesInfoDataSet result = null;
    if (infoMap != null) {
      result = new DataNodesInfoDataSet();
      for (Map.Entry<Integer, DataNodeInfo> entry : infoMap.entrySet()) {
        result.addDataNodeInfo(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }

  private StorageGroupSchemaDataSet queryStorageGroupSchema() {
    List<StorageGroupSchema> schemaList = partitionTable.getStorageGroupSchema();
    StorageGroupSchemaDataSet result = null;
    if (schemaList != null) {
      result = new StorageGroupSchemaDataSet(schemaList);
    }
    return result;
  }
}
