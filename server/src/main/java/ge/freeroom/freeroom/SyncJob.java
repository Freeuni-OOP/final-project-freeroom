package ge.freeroom.freeroom;

import ge.freeroom.freeroom.service.LectureSyncService;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class SyncJob {
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("Booting up Sync Job...");
        System.out.println("=========================================");
        
        ConfigurableApplicationContext context = new SpringApplicationBuilder(FreeroomApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);

        LectureSyncService syncService = context.getBean(LectureSyncService.class);
        
        System.out.println("=========================================");
        System.out.println("Starting Google Calendar Sync...");
        System.out.println("=========================================");
        
        try {
            syncService.syncAllRooms();
            System.out.println("=========================================");
            System.out.println("Sync completed successfully!");
            System.out.println("=========================================");
        } catch (Exception e) {
            System.err.println("=========================================");
            System.err.println("Sync FAILED!");
            e.printStackTrace();
            System.err.println("=========================================");
        } finally {
            context.close();
            System.exit(0);
        }
    }
}
