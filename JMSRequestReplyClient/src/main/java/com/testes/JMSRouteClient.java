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
		
		from("jms:mySincQueue?concurrentConsumers=50")
		.log("Retirando da Fila: ${body}")
		.process( ex -> {		
			System.out.println(Arrays.toString(ex.getIn().getHeaders().entrySet().toArray()));
			
				try {
					Thread.sleep(10 * 1000);
				} catch (Exception ex1){
				}

		})
		
		.process( ex -> {
			
			Random r = new Random();
			int n = r.nextInt(9999);
			ex.getIn().setHeader("random", n);
			
			/*
			String queue = ex.getIn().getHeader("JMSReplyTo").toString();
			queue = queue.substring( queue.indexOf("//") + 2 );
			System.out.println("Fila de retorno: " + queue);
			
			ex.getIn().setHeader("JMSReplyTo" , null); //importante colocar null senão o JMS tenta enviar automaticamente;			
			ex.setProperty("fila_de_retorno", queue);
			*/

			ex.setProperty("fila_de_retorno", ex.getIn().getHeader("fila_para_retorno").toString());
			System.out.println( ex.getProperty("fila_de_retorno") );
			
		})
		.transform().simple("retorno total ${body}${body}-${header.random}!;${body}")
		.toD("jms://${exchangeProperty.fila_de_retorno}"); //fila para retorno. só necessário colocar em rotas InOnly
		
	}

}
