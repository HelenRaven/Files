import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {
    private static final String ROOT_FOLDER_PATH = "./Games";
    private static final String LOG_PATH = ROOT_FOLDER_PATH + "/temp/temp.txt";
    private static final String SAVE_PATH =  ROOT_FOLDER_PATH + "/savegames";

    public static void main(String[] args) throws IOException {
        String[] dirArr = {
                "/src",
                "/res",
                "/savegames",
                "/temp",
                "/src/main",
                "/src/test",
                "/res/drawables",
                "/res/vectors",
                "/res/icons",
                "/temp/temp.txt",
                "/src/main/Main.java",
                "/src/main/Utils.java"
        };
        StringBuilder sb = new StringBuilder();

        for (String path : dirArr) {
            if (path.contains(".")) {
                makeFile(ROOT_FOLDER_PATH + path, sb);
            } else {
                makeDir(ROOT_FOLDER_PATH + path, sb);
            }
        }

        File file = new File(LOG_PATH);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.append(sb);
        }

        // GameProgress serialization

        GameProgress[] gameProgress = {
                new GameProgress(94, 10, 2, 254.32),
                new GameProgress(92, 15, 3, 399.98),
                new GameProgress(85, 25, 5, 550)
        };

        List<String> archList = new ArrayList<String>();

        for(int i = 1; i <= gameProgress.length; i++){
            String path = SAVE_PATH + "/data" + i + ".dat";
            archList.add(path);
            SaveGame(path, gameProgress[i - 1]);
        }

        String zipPath = SAVE_PATH + "/zip.zip";
        zipFiles(zipPath, archList);
        clearSave(SAVE_PATH, archList);

        // GameProgress deserialization

        openZip(zipPath, SAVE_PATH);
        System.out.println(deserializeData(archList.get(1)));
    }

    private static void makeDir(String path, StringBuilder log) {
        if (path == null) {
            return;
        }

        File dir = new File(path);
        if (dir.mkdir()) {
            log.append("\n[ " + LocalDate.now() + " ]" + " INFO -- : Directory created: " + path);
        }
    }

    private static void makeFile(String path, StringBuilder log) {
        if (path == null) {
            return;
        }

        File file = new File(path);
        try {
            if (file.createNewFile())
                log.append("\n[ " + LocalDate.now() + " ]" + " INFO -- : File created: " + path);
        } catch (IOException e) {
            log.append("\n[ " + LocalDate.now() + " ]" + " ERROR -- : File creation failed: " + path + e.getMessage());
        }
    }

    private static void SaveGame(String path, GameProgress gp) {
        if (path == null || gp == null) {
            return;
        }

        try(FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(gp);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void zipFiles(String path, List<String> list) {
        if (list == null || list.isEmpty() || path == null) {
            return;
        }

        try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(path))) {
            for (String filePath : list) {
                try(FileInputStream fis = new FileInputStream(filePath)) {
                    ZipEntry entry = new ZipEntry(filePath);
                    zout.putNextEntry(entry);
                    byte[] buffer = new byte[fis.available()];
                    fis.read(buffer);
                    zout.write(buffer);
                    zout.closeEntry();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void clearSave(String path, List<String> list) throws IOException {
        if (list == null || list.isEmpty() || path == null) {
            return;
        }

        File source = new File(path);

        for (File file : source.listFiles()) {
            if (file.isFile() && list.contains(file.getPath().replace('\\', '/')) ) {
                file.delete();
            }
        }
    }

    private static void openZip(String zipPath, String folderPath) {
        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry entry;
            String[] name;

            while((entry = zis.getNextEntry()) != null){
                name = entry.getName().split("/");
                FileOutputStream fout = new FileOutputStream(folderPath + "/" + name[name.length - 1]);
                for(int i = zis.read(); i != -1; i = zis.read()) {
                    fout.write(i);
                }
                fout.flush();
                zis.closeEntry();
                fout.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static GameProgress deserializeData(String path) {
        try(FileInputStream fis = new FileInputStream(path);
        ObjectInputStream ois = new ObjectInputStream(fis)) {
           return (GameProgress) ois.readObject();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
