/**
 * Copyright (C) 2014-2016 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.pinot.core.operator.aggregation.function;

import com.linkedin.pinot.core.operator.aggregation.AggregationResultHolder;
import com.linkedin.pinot.core.operator.aggregation.groupby.GroupByResultHolder;
import com.linkedin.pinot.core.operator.docvalsets.ProjectionBlockValSet;
import com.linkedin.pinot.core.query.aggregation.function.quantile.digest.QuantileDigest;
import javax.annotation.Nonnull;


public class PercentileEstMVAggregationFunction extends PercentileEstAggregationFunction {
  private final String _name;

  public PercentileEstMVAggregationFunction(int percentile) {
    super(percentile);
    switch (percentile) {
      case 50:
        _name = AggregationFunctionFactory.PERCENTILEEST50_MV_AGGREGATION_FUNCTION;
        break;
      case 90:
        _name = AggregationFunctionFactory.PERCENTILEEST90_MV_AGGREGATION_FUNCTION;
        break;
      case 95:
        _name = AggregationFunctionFactory.PERCENTILEEST95_MV_AGGREGATION_FUNCTION;
        break;
      case 99:
        _name = AggregationFunctionFactory.PERCENTILEEST99_MV_AGGREGATION_FUNCTION;
        break;
      default:
        throw new UnsupportedOperationException(
            "Unsupported percentile for PercentileEstMVAggregationFunction: " + percentile);
    }
  }

  @Nonnull
  @Override
  public String getName() {
    return _name;
  }

  @Override
  public void aggregate(int length, @Nonnull AggregationResultHolder aggregationResultHolder,
      @Nonnull ProjectionBlockValSet... projectionBlockValSets) {
    double[][] valuesArray = projectionBlockValSets[0].getMultiValues();
    QuantileDigest quantileDigest = aggregationResultHolder.getResult();
    if (quantileDigest == null) {
      quantileDigest = new QuantileDigest(DEFAULT_MAX_ERROR);
      aggregationResultHolder.setValue(quantileDigest);
    }
    for (int i = 0; i < length; i++) {
      for (double value : valuesArray[i]) {
        quantileDigest.add((long) value);
      }
    }
  }

  @Override
  public void aggregateGroupBySV(int length, @Nonnull int[] groupKeyArray,
      @Nonnull GroupByResultHolder groupByResultHolder, @Nonnull ProjectionBlockValSet... projectionBlockValSets) {
    double[][] valuesArray = projectionBlockValSets[0].getMultiValues();
    for (int i = 0; i < length; i++) {
      int groupKey = groupKeyArray[i];
      QuantileDigest quantileDigest = groupByResultHolder.getResult(groupKey);
      if (quantileDigest == null) {
        quantileDigest = new QuantileDigest(DEFAULT_MAX_ERROR);
        groupByResultHolder.setValueForKey(groupKey, quantileDigest);
      }
      for (double value : valuesArray[i]) {
        quantileDigest.add((long) value);
      }
    }
  }

  @Override
  public void aggregateGroupByMV(int length, @Nonnull int[][] groupKeysArray,
      @Nonnull GroupByResultHolder groupByResultHolder, @Nonnull ProjectionBlockValSet... projectionBlockValSets) {
    double[][] valuesArray = projectionBlockValSets[0].getMultiValues();
    for (int i = 0; i < length; i++) {
      double[] values = valuesArray[i];
      for (int groupKey : groupKeysArray[i]) {
        QuantileDigest quantileDigest = groupByResultHolder.getResult(groupKey);
        if (quantileDigest == null) {
          quantileDigest = new QuantileDigest(DEFAULT_MAX_ERROR);
          groupByResultHolder.setValueForKey(groupKey, quantileDigest);
        }
        for (double value : values) {
          quantileDigest.add((long) value);
        }
      }
    }
  }
}
