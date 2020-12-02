package com.aniamadej.sublokator;

import com.aniamadej.sublokator.model.MediumConnection;
import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.repository.MediumConnectionRepository;
import com.aniamadej.sublokator.repository.MediumMeterRepository;
import com.aniamadej.sublokator.service.MediumConnectionService;
import com.aniamadej.sublokator.service.MediumMeterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SublokatorApplicationTests {

	@Autowired
	private MediumMeterService mediumMeterService;


	@Autowired
	private MediumConnectionService mediumConnectionService;

	@Autowired
	private MediumMeterRepository mmrep;

	@Autowired
	private MediumConnectionRepository mcrep;


	@Test
	void contextLoads() {
	}

	@Test
	void addsMediumMeterToDatabase(){
//		MediumMeter mediumMeter = new MediumMeter("2222", "kwh");
//
//		MediumConnection mediumConnection = new MediumConnection("gaz");
//
//		mediumConnection.addMediumMeter(mediumMeter);
//		mediumConnection.addMediumMeter(new MediumMeter("212333", "wkw"));
//
//		mediumConnectionService.save(mediumConnection);
//		mediumConnection = new MediumConnection("prąd");
//		mediumConnection.addMediumMeter(new MediumMeter("lolek", "dupy"));
//		mediumConnectionService.save(mediumConnection);
//
//
//		mediumConnectionService.getNamesList().forEach(n -> System.out.println(n.getName()));

//		MediumConnection mc = mcrep.findById(1L).get();
//		mcrep.delete(mc);

		MediumMeter mediumMeter = new MediumMeter("wqqwe", "qweqwe");
		MediumConnection mc = mcrep.findById(4L).get();
		mc.addMediumMeter(mediumMeter);
		mcrep.save(mc);
	}

}
