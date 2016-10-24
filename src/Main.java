import java.io.File;

public class Main {

    public static void main(String[] args) {
        File fileDir = new File("C:\\Users\\Evgenia\\IdeaProjects\\OTIKLab2\\Texts");
        File[] files = fileDir.listFiles();
        for(File f : files){
            CodingClass codingClass = new CodingClass();
            codingClass.countDelRepeats(codingClass.readFromFile(f.getPath()));
            codingClass.returnCodeResults();
        }

    }
}
