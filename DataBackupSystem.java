import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.text.SimpleDateFormat;

// Serializable BackupData class
class BackupData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String backupId;
    private Date backupTime;
    private String description;
    private HashMap<String, Object> data;

    public BackupData(String description, HashMap<String, Object> data) {

        this.backupId = UUID.randomUUID().toString();
        this.backupTime = new Date();
        this.description = description;
        this.data = new HashMap<>(data);
    }

    public String getBackupId() {
        return backupId;
    }

    public Date getBackupTime() {
        return backupTime;
    }

    public String getDescription() {
        return description;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    // Save binary
    public void saveBinary(String path) throws Exception {

        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));

        oos.writeObject(this);
        oos.close();
    }

    // Save compressed
    public void saveCompressed(String path) throws Exception {

        ObjectOutputStream oos = new ObjectOutputStream(
                new GZIPOutputStream(
                        new FileOutputStream(path)));

        oos.writeObject(this);
        oos.close();
    }

    // Save CSV
    public void saveCSV(String path) throws Exception {

        BufferedWriter writer = new BufferedWriter(new FileWriter(path));

        writer.write("Key,Value\n");

        for (String key : data.keySet()) {

            writer.write(key + "," + data.get(key));
            writer.newLine();
        }

        writer.close();
    }

    @Override
    public String toString() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return "\nBackup ID: " + backupId +
                "\nTime: " + sdf.format(backupTime) +
                "\nDescription: " + description +
                "\nItems: " + data.size();
    }
}

// Backup Manager
class BackupManager {

    static final String BACKUP_DIR = "backups/";

    public BackupManager() {

        File dir = new File(BACKUP_DIR);

        if (!dir.exists())
            dir.mkdir();
    }

    // Create backup
    public void createBackup(String description,
            HashMap<String, Object> data)
            throws Exception {

        BackupData backup = new BackupData(description, data);

        String time = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());

        String base = BACKUP_DIR + "backup_" + time;

        backup.saveBinary(base + ".dat");

        backup.saveCompressed(base + ".dat.gz");

        backup.saveCSV(base + ".csv");

        System.out.println("Backup created!");
    }

    // Restore binary
    public BackupData restoreBinary(String path)
            throws Exception {

        ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(path));

        BackupData data = (BackupData) ois.readObject();

        ois.close();

        return data;
    }

    // Restore compressed
    public BackupData restoreCompressed(String path)
            throws Exception {

        ObjectInputStream ois = new ObjectInputStream(
                new GZIPInputStream(
                        new FileInputStream(path)));

        BackupData data = (BackupData) ois.readObject();

        ois.close();

        return data;
    }

    // List backups
    public void listBackups() {

        File folder = new File(BACKUP_DIR);

        File[] files = folder.listFiles();

        if (files == null || files.length == 0) {

            System.out.println("No backups.");
            return;
        }

        for (File f : files)
            System.out.println(f.getName());
    }

    // Delete old backups
    public void cleanup(int days) {

        File folder = new File(BACKUP_DIR);

        long cutoff = System.currentTimeMillis()
                - days * 24L * 60 * 60 * 1000;

        File[] files = folder.listFiles();

        if (files == null)
            return;

        for (File f : files) {

            if (f.lastModified() < cutoff) {

                f.delete();
                System.out.println("Deleted: "
                        + f.getName());
            }
        }
    }

}

// Main system
public class DataBackupSystem {

    static Scanner sc = new Scanner(System.in);

    static BackupManager manager = new BackupManager();

    public static void main(String[] args)
            throws Exception {

        int choice;

        do {

            System.out.println("\n=== DATA BACKUP SYSTEM ===");

            System.out.println("1 Create Backup");

            System.out.println("2 Restore Binary");

            System.out.println("3 Restore Compressed");

            System.out.println("4 List Backups");

            System.out.println("5 Cleanup");

            System.out.println("6 Exit");

            System.out.print("Choice: ");

            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:
                    createBackup();
                    break;

                case 2:
                    restoreBinary();
                    break;

                case 3:
                    restoreCompressed();
                    break;

                case 4:
                    manager.listBackups();
                    break;

                case 5:
                    cleanup();
                    break;

                case 6:
                    System.out.println("Exit");
                    break;
            }

        } while (choice != 6);
    }

    static void createBackup() throws Exception {

        System.out.print("Description: ");

        String desc = sc.nextLine();

        HashMap<String, Object> data = new HashMap<>();

        String key;

        do {

            System.out.print("Enter key: ");
            key = sc.nextLine();

            System.out.print("Enter value: ");
            String value = sc.nextLine();

            data.put(key, value);

            System.out.print("Add more (y/n): ");

        } while (sc.nextLine().equals("y"));

        manager.createBackup(desc, data);
    }

    static void restoreBinary() throws Exception {

        System.out.print("Enter file path: ");

        String path = sc.nextLine();

        BackupData data = manager.restoreBinary(path);

        System.out.println(data);
    }

    static void restoreCompressed()
            throws Exception {

        System.out.print("Enter file path: ");

        String path = sc.nextLine();

        BackupData data = manager.restoreCompressed(path);

        System.out.println(data);
    }

    static void cleanup() {

        System.out.print("Enter days: ");

        int days = sc.nextInt();

        manager.cleanup(days);
    }

}
