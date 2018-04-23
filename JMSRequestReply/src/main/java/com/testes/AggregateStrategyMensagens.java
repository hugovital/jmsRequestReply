package com.testes;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

public class AggregateStrategyMensagens implements AggregationStrategy {

	@Override
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
		
		if (oldExchange == null)
			return newExchange;
		else {		
			oldExchange.getIn().setBody( newExchange.getIn().getBody() );
			return oldExchange;
		}

	}

}
