package com.testes;

import java.util.Random;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.sjms.SjmsComponent;

public class JMSRoute extends RouteBuilder {
	
	public static int time = 0;

	@Override
	public void configure() throws Exception {
		
		SjmsComponent component = new SjmsComponent();
		ActiveMQConnectionFactory fac = new ActiveMQConnectionFactory("tcp://localhost:61616");
		fac.setUserName("admin");
		fac.setPassword("admin");

		component.setConnectionFactory(fac);
		getContext().addComponent("sjms", component);		
		
		from("stream:in")
			.log("${body}")
			.split(body().tokenize(","))			
			.wireTap("direct:toFila");

		Random r = new Random();
		int n = r.nextInt(999);
		
		n = 775;
		
		from("direct:toFila")
			.setProperty("guardado", simple("${body}"))
			//.to("jms:mySincQueue?exchangePattern=InOut&requestTimeout=2000000")
			.to("sjms:mySincQueue?exchangePattern=InOut&responseTimeOut=2000000&namedReplyTo=returnQueue_" + n)
			.log("Recebido da Junção: ${exchangeProperty.guardado} - ${body}");
		
		/**
		 * Cliente
		 */
//		from("jms:mySincQueue?concurrentConsumers=5")
//			.log("Retirando da Fila: ${body}")
//			.process( ex -> {		
//				System.out.println(Arrays.toString(ex.getIn().getHeaders().entrySet().toArray()));
//				
//					try {
//						JMSRoute.time++;
//						if (JMSRoute.time % 2 == 0){							
//							Thread.sleep(30*1000);
//						} else {
//							Thread.sleep(1*1000);
//						}
//					} catch (Exception ex1){
//					}
//
//			})
//			
//			.process( ex -> {
//				
//				Random r = new Random();
//				int n = r.nextInt(9999);
//				ex.getIn().setHeader("random", n);
//				
//			})
//			.transform().simple("retorno total ${body}${body}-${header.random} !");
			
		
	}

}
