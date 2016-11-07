import java.io.File;

public class Main {

    public static void main(String[] args) {
            CodingClass codingClass = new CodingClass();
            codingClass.countDelRepeats(codingClass.readFromFile("C:\\Users\\Evgenia\\IdeaProjects\\OTIKLab2\\Texts\\text1"));
            codingClass.codeFile();
            codingClass.PrintRes();

        codingClass.decodeFile();

    }
}
