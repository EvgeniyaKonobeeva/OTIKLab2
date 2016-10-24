import java.io.*;
import java.util.*;

/**
 * Created by Evgenia on 08.10.2016.
 */
public class CodingClass {
    private Map<Byte[], Double> byteFreqMap = new HashMap<>();

    private Map<Byte, Double> byteFreqMapConst = new HashMap<>();

    private Map<Byte, ArrayList<Integer>> resultBytesCode = new HashMap<>();

    private String fileName;

    private String filePath;


    public byte[] readFromFile(String filePath){
        this.filePath = filePath;
        fileName = new File(filePath).getName();
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        InputStream input = null;
        try {
            input = new BufferedInputStream(new FileInputStream(filePath));
            int data = 0;
            while ((data = input.read()) != -1) {
                byteStream.write(data);
            }
        }catch (IOException ioe){
            ioe.printStackTrace();
        }

        if(byteStream.size()!= 0){
            return byteStream.toByteArray();

        }else{
            System.out.println("empty File");
            return null;
        }
    }


    public void countDelRepeats(byte[] byteArr){

        ArrayList<Byte> chars = new ArrayList<>();

        for(int i = 0; i < byteArr.length; i++){
            chars.add(byteArr[i]);
        }
        for(int i = 0; i < chars.size(); i++){
            int countSym = 1;
            Byte b = chars.get(i);
            for(int j = i+1; j < chars.size(); j++){
                if(b == chars.get(j)){
                    chars.remove(j--);
                    countSym++;
                }
            }
            byteFreqMap.put(new Byte[]{b}, (double)countSym);
            byteFreqMapConst.put(b, (double)countSym);
        }

        generateResultArr(chars);
    }

    public void generateResultArr(ArrayList<Byte> byteArr){

        for(int i = 0; i < byteArr.size(); i++){
            ArrayList<Integer> resCode = new ArrayList<>();
            resultBytesCode.put(byteArr.get(i), resCode);
        }
        findCode();

    }

    public void findCode(){
        while (byteFreqMap.entrySet().size() > 1){
            findTwoMinimals(byteFreqMap);
        }
    }

    public void findTwoMinimals(Map<Byte[], Double> map){
        Byte[] minByte1 = null;
        Byte[] minByte2 = null;
        double freq1 = Double.MAX_VALUE;
        double freq2 = Double.MAX_VALUE;
        for(Map.Entry<Byte[], Double> entry : map.entrySet()){
            if(freq1 > entry.getValue()){
                freq1 = entry.getValue();
                minByte1 = entry.getKey();
            }
        }

        for(Map.Entry<Byte[], Double> entry : map.entrySet()){
            if(freq2 > entry.getValue() && !minByte1.equals(entry.getKey())){
                freq2 = entry.getValue();
                minByte2 = entry.getKey();
            }
        }
        map.remove(minByte1);
        map.remove(minByte2);

        Byte[] s = concateArrs(minByte1, minByte2);
        map.put(s, freq1+freq2);

        splitIntoBytesAndPut(minByte1, 0);
        splitIntoBytesAndPut(minByte2, 1);

    }

    protected Byte[] concateArrs(Byte[] bar1, Byte[] bar2){
        int size = bar1.length + bar2.length;
        Byte[] resar = new Byte[size];
        for(int i = 0; i < bar1.length; i++){
            resar[i] = bar1[i];
        }
        int j = 0;
        for(int i = bar1.length; i < size; i++){
            resar[i] = bar2[j++];
        }
        return resar;
    }

    public void splitIntoBytesAndPut(Byte[] bar, int num){
        for(int i = 0; i < bar.length; i++){
            if(resultBytesCode.containsKey(bar[i])){
                resultBytesCode.get(bar[i]).add(num);
            }else System.out.println("not contain byte " + bar[i]);
        }

    }

    public void PrintRes(){
        for(Map.Entry<Byte, ArrayList<Integer>> entry : resultBytesCode.entrySet()){
            System.out.println(Integer.toHexString(Byte.toUnsignedInt(entry.getKey())) + " " + entry.getValue());
        }
    }

    public void returnCodeResults(){
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(new File("C:\\Users\\Evgenia\\IdeaProjects\\OTIKLab2\\ResultsCode\\" + fileName), true);
            for(Map.Entry<Byte, ArrayList<Integer>> entry : resultBytesCode.entrySet()){
                fileWriter.append(Integer.toHexString(Byte.toUnsignedInt(entry.getKey())) + " " + entry.getValue() + "\n");
                fileWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void codeFile(){
        byte[] arr = readFromFile(filePath);
        for(int i = 0; i < arr.length; i++){
            if(resultBytesCode.containsKey(arr[i])){
                FileWriter fileWriter = null;
                try {
                    fileWriter = new FileWriter(new File("C:\\Users\\Evgenia\\IdeaProjects\\OTIKLab2\\ResultsCode\\" + fileName), true);
                    fileWriter.append(resultBytesCode.get(arr[i]).toString());
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    protected void

}
