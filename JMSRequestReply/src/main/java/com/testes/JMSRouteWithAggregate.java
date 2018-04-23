package com.testes;

import java.util.Random;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.sjms.SjmsComponent;

public class JMSRouteWithAggregate extends RouteBuilder {

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
		
		String filaRetorno = "returnQueue_" + n;

		from("direct:toFila")
			.setProperty("guardado", simple("${body}"))
			//.setHeader("JMSReplyTo", constant("returnQueue_775"))
			.setHeader("fila_para_retorno", constant( filaRetorno ))
			.to("sjms:mySincQueue?exchangePattern=InOnly")
			.to("direct:aggregate");
		
		//----retorno

		//from("jms:returnQueue_775")
		from("jms:" + filaRetorno)
			.log("removido da fila: ${body}")
			.process( ex -> {
				
				String s = (String) ex.getIn().getBody();
				s = s.substring( s.indexOf(";")+1 );
				System.out.println(s);
				ex.setProperty("guardado", s);

			})			
			.to("direct:aggregate");
		
		
		//---aggregate
		from("direct:aggregate")
			.log("dentro do aggregate")
			.aggregate( simple(" ${exchangeProperty.guardado}" ), new AggregateStrategyMensagens() )
			.completionSize( 2 )
			.completionTimeout( 20000 )
			.to("direct:fimAgregacao");
		
		//----fim aggregate
		from("direct:fimAgregacao")
			.log("Recebido da Junção: ${header.CamelAggregatedCompletedBy} - ${exchangeProperty.guardado} - ${body}");
		
	}

}
