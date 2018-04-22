package com.testes;

import java.util.Arrays;
import java.util.Random;

import org.apache.camel.builder.RouteBuilder;

public class JMSRouteClient extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		
		errorHandler(
				deadLetterChannel("direct:errors").maximumRedeliveries(10)
		);
		
		from("direct:errors")
			.log("...dentro de erros....");
		
		from("jms:mySincQueue?concurrentConsumers=5")
		.log("Retirando da Fila: ${body}")
		.process( ex -> {		
			System.out.println(Arrays.toString(ex.getIn().getHeaders().entrySet().toArray()));
			
				try {
					Thread.sleep(60 * 1000);
				} catch (Exception ex1){
				}

		})
		
		.process( ex -> {
			
			Random r = new Random();
			int n = r.nextInt(9999);
			ex.getIn().setHeader("random", n);
			
		})
		.transform().simple("retorno total ${body}${body}-${header.random} !");
		
	}

}
