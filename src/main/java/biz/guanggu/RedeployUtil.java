package biz.guanggu;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;

import javax.servlet.ServletContext;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;

/**
 * Created by Li He on 2014/6/13.
 *
 * @author Li He
 */
public class RedeployUtil {

    public static void main(String[] args) throws IOException, ArchiveException {

    }

    public static void deploy(ServletContext context, String srcWarPath, String middleWarPath, String... distWarPaths){
        ErrorLogger logger = new ErrorLogger();
        logger.startDeploy();

        try{
            if(move(logger, srcWarPath, middleWarPath)){
                distribute(logger, middleWarPath, distWarPaths);
            }else{
                logger.log("redeploy worker need to do nothing at all!");
            }
        } catch (IOException e) {
            logger.log(e.getMessage()+"\n"+ Arrays.toString(e.getStackTrace()));
        } catch (ArchiveException e) {
            logger.log(e.getMessage()+"\n"+ Arrays.toString(e.getStackTrace()));
        } catch (Exception e) {
            logger.log(e.getMessage()+"\n"+ Arrays.toString(e.getStackTrace()));
        } finally {
            logger.endDeploy();
            synchronized (context.getAttribute("redeploying")){
                context.setAttribute("redeploying", Boolean.FALSE);
            }
        }
    }

    public static boolean move(ErrorLogger logger, String srcFilePath, String desFilePath) throws IOException {

        Path sourcePath = Paths.get(srcFilePath),
                destinationPath = Paths.get(desFilePath);

        boolean copyFlag = true;

        if (Files.exists(destinationPath)) {
            FileTime srcFileTime = (FileTime) Files.getAttribute(sourcePath, "lastModifiedTime"),
                    desFileTime = (FileTime) Files.getAttribute(destinationPath, "lastModifiedTime");

            if (srcFileTime.compareTo(desFileTime) < 1) {
                copyFlag = false;
            }
        }

        if (copyFlag) {
            logger.log("start to move from '"+srcFilePath+"' to '"+desFilePath+"'...");
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            logger.log("end move...");
        }

        return copyFlag;
    }

    public static void distribute(ErrorLogger logger, String srcFilePath, String... desFilePaths) throws IOException, ArchiveException {

        Path sourcePath = Paths.get(srcFilePath);

        for (String desFilePath : desFilePaths) {
            logger.log("start to copy war to '"+desFilePath+"'...");
            Files.copy(sourcePath, Paths.get(desFilePath), StandardCopyOption.REPLACE_EXISTING);
            logger.log("end copy...");

            if (desFilePath.endsWith(".war")) {
                Path folderPath = Paths.get(desFilePath.substring(0, desFilePath.indexOf(".war")));

                if (Files.exists(folderPath)) {
                    logger.log(folderPath.toString() + " exists! So iterate it and then delete all!");
                    Files.walkFileTree(folderPath, new DeleteDirectory());
                    //Files.deleteIfExists(folderPath);
                    logger.log("finish delete operation...");
                }

                logger.log("start to unzip war to folder "+folderPath.toString()+"...");
                unzip(desFilePath, folderPath.toString());
                logger.log("end unzip war...");

                logger.log("reconfigure the project...");
                Files.copy(Paths.get(folderPath.toString() + ".properties"), Paths.get(folderPath.toString(), "\\WEB-INF\\classes\\project-custom.properties"), StandardCopyOption.REPLACE_EXISTING);
                logger.log("end reconfigure...");
            }
        }

    }

    /**
     * unzip war
     *
     * @param warPath   war path
     * @param unzipPath unzip directory path
     */
    public static void unzip(String warPath, String unzipPath) throws IOException, ArchiveException {
        File warFile = new File(warPath);

        //获得输出流
        BufferedInputStream bufferedInputStream = new BufferedInputStream(
                new FileInputStream(warFile));
        ArchiveInputStream in = new ArchiveStreamFactory()
                .createArchiveInputStream(ArchiveStreamFactory.JAR,
                        bufferedInputStream);
        JarArchiveEntry entry;
        //循环遍历解压
        while ((entry = (JarArchiveEntry) in.getNextEntry()) != null) {
            //解压到当前目录下
            String entryName = entry.getName();
//            if(entryName.indexOf("/")==(entryName.length()-1))
//                continue;

//            entryName = entryName.substring(entryName.indexOf("/")+1);

            File file = new File(unzipPath, entryName);

            if(file.exists()&&file.getName().endsWith(".jar"))
                continue;

            if (entry.isDirectory()) {
                file.mkdir();
            } else {
                OutputStream out = FileUtils.openOutputStream(file);
                IOUtils.copy(in, out);
                out.close();
            }
        }
        in.close();

    }


}
