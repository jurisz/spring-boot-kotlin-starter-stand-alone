package tools;


import com.twino.ls.app.BootApplication;
import org.springframework.boot.SpringApplication;

public class BootApplicationLauncher {

	public static void main(final String[] args) throws Exception {
		System.setProperty("net.sf.ehcache.skipUpdateCheck", "true");
		SpringApplication.run(BootApplication.class, args);
	}
}
