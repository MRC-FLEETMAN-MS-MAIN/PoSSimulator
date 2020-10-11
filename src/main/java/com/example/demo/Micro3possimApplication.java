package com.example.demo;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

//import org.springframework.jms.core.JmsTemplate;

@SpringBootApplication
public class Micro3possimApplication {
	
	

	
	
	private static Map<String, List<String>> reports;
	
	
	 @Value("${queue.name}")
	   private String queuename;
		
		
		
		@Bean
		public Queue queue() {
			return new Queue(queuename,true);
		}
		
	


	public static void main(String[] args) throws IOException {
		
	
		
		loadData();
		
		 
		
		ConfigurableApplicationContext ctx =  SpringApplication.run(Micro3possimApplication.class, args);

		//JmsTemplate template = ctx.getBean(JmsTemplate.class);
		RabbitTemplate rabbitTemplate = ctx.getBean(RabbitTemplate.class);
		String queuename = "Fleetman";
		Queue rabqueue = new Queue(queuename,true);
		ObjectMapper mapper = new ObjectMapper();
		/*
		Iterator<List<String>> itr = reports.values().iterator();
		while (itr.hasNext()) {
			System.out.println(itr.next());
		}  */
		
		
		
		
		while(!reports.isEmpty()) {
			for (String key : reports.keySet()) {
				
				List<String> thisVehiclesReports = reports.get(key);
				String report = thisVehiclesReports.remove(0);
				
				if(reports.get(key).isEmpty()) {
					reports.remove(key);
					System.out.println("Trip seems to be over....Reverting");
					loadData();
					continue;
				}
				
				String[] posdata = report.split("\"");
				String lat = posdata[1];
				String longitude = posdata[3];
				
				//System.out.printf("The lat is %s and the long is %s",lat,longitude);
				
				//HashMap<String,String> positionMessage = new HashMap<>();
				ObjectNode positionMessage = mapper.createObjectNode(); //serialising java object to JSON using Jackson Mapper
				positionMessage.put("vehicle", key);
				positionMessage.put("lat", lat);
				positionMessage.put("long", longitude);
				
				
				boolean messagenotsent = true;
				
				while(messagenotsent) {
					try {
						
						rabbitTemplate.convertAndSend(rabqueue.getName(), positionMessage.toString());
						messagenotsent = false;
					}
					
					catch(Exception e) {
						System.out.println("POSTSSS");
						System.out.println(e);
						wait(5000);
					}
				}
				
				wait(300);
				
				
			}
			
			
		}
		
		
	}
	
	
	private static void loadData() throws IOException {
		
		reports = new HashMap<>();
		PathMatchingResourcePatternResolver path = new PathMatchingResourcePatternResolver();
		for (Resource nextFile : path.getResources("tracks/*")) {

			URL resource = nextFile.getURL();
			File f = new File(resource.getFile());
			String vehiclename = f.getName();
			InputStream is = Micro3possimApplication.class.getResourceAsStream("/tracks/"+ f.getName());

			//String vehiclename = nextFile.getFilename();
			try {
				//Scanner sc = new Scanner(nextFile.getFile());
				Scanner sc = new Scanner(is);
				List<String> vehicleposdata = new ArrayList<>();
				while (sc.hasNextLine()) {
					vehicleposdata.add(sc.nextLine());
				}

				reports.put(vehiclename, vehicleposdata);
			} catch (Exception e) {
				System.out.println(e);
			}
		}


}
	
	public static void wait(int ms)
	{
	    try
	    {
	        Thread.sleep(ms);
	    }
	    catch(InterruptedException ex)
	    {
	        Thread.currentThread().interrupt();
	    }
	}
	
	
		 
}
