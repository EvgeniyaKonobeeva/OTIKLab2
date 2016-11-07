import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Evgenia on 08.10.2016.
 */
public class CodingClass {
    private Map<Byte[], Double> byteFreqMap = new HashMap<>();

    private Map<Byte, Double> byteFreqMapConst = new HashMap<>();

    private Map<Byte, ArrayList<Integer>> resultBytesCode = new HashMap<>();

    private String fileName;

    private String filePath;

    String msg ;

    byte[] readArr;

    public CodingClass(int mode, String filePath){
        this.filePath = filePath;
        fileName = new File(filePath).getName();
    }


    public byte[] readFromFile(String filePath){

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
            System.out.println(entry.getKey() + " " + soutArr(entry.getValue()));
        }
    }
    private String soutArr(ArrayList list){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < list.size(); i++){
            sb.append(list.get(i) + ", ");
        }
        return sb.toString();
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
        readArr = readFromFile(filePath);
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < readArr.length; i++){
            if(resultBytesCode.containsKey(readArr[i])){
                sb.append(resultBytesCode.get(readArr[i]).toString().replaceAll("[,\\]\\[\\s]*", ""));
            }else{
                System.out.println("not contain byte " + readArr[i]);
            }
        }

        msg = sb.toString();
        DataOutputStream os = null;
        try {
            os = new DataOutputStream(new FileOutputStream("C:\\Users\\Evgenia\\IdeaProjects\\OTIKLab2\\ResultsCode\\ttt3.bin"));
            byte[] b = codeKeysMapToBytes(resultBytesCode);
            int size = b.length;
            os.write(((Integer)size).byteValue());
            os.write(codeKeysMapToBytes(resultBytesCode));
            os.write(getBits(sb.toString()));
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getBits(String str) {
        String s = "00000000";
        int strSize = str.length();
        int byteArrSize = (int) Math.ceil((double) strSize / 8.0);
        int remainder = strSize % 8;
        str = str + s.substring(0, 8 - remainder);
        byte[] b = new byte[byteArrSize];
        int count = 0;
        for (int i = 0; i < str.length(); i += 8) {
            int val = Integer.parseInt(str.substring(i, i + 8), 2);
            b[count++] = ((Integer) val).byteValue();

        }
        return b;
    }

    public byte[] codeKeysMapToBytes( Map<Byte, ArrayList<Integer>> map){
        ArrayList<Byte> b = new ArrayList<>();
        for(Map.Entry<Byte, ArrayList<Integer>> entry : map.entrySet()){
            b.add(entry.getKey());
            b.add(((Integer)entry.getValue().size()).byteValue());
            b.add(getBits(entry.getValue().toString().replaceAll("[,\\]\\[\\s]*",""))[0]);
        }
        byte[] bytes = new byte[b.size()];
        for(int i = 0; i < b.size(); i++){
            bytes[i] = b.get(i);
        }
        return bytes;
    }



    Map<Byte, String> decodeTree = new HashMap<>();
    public void decodeFile(){
        byte[] fileByteArr = readFromFile("C:\\Users\\Evgenia\\IdeaProjects\\OTIKLab2\\ResultsCode\\ttt3.bin");
//        String s1 = Integer.toBinaryString(((Byte)(fileByteArr[0])).intValue());
        int treeSize = Byte.toUnsignedInt(fileByteArr[0]);
//        String s1 = Integer.toHexString(treeSize);
        for(int i = 1; i < treeSize+1; i+=3){
            int bits = Byte.toUnsignedInt(fileByteArr[i+1]);
            String s1 = Integer.toBinaryString(Byte.toUnsignedInt(fileByteArr[i+2]));
            String s2 = getFull8bitByte(s1).substring(0, bits);
            decodeTree.put(fileByteArr[i], s2);
        }
        decodeTree.size();
        readMsg(fileByteArr, treeSize);
    }
    private String getFull8bitByte(String s){
        if(s.length() < 8){
            int r = 8-s.length();
            StringBuilder sb = new StringBuilder();
            for(int k = 0; k < r; k++){
                sb.append("0");
            }
            sb.append(s);
            s = sb.toString();
        }
        return s;
    }

    ArrayList<Byte> resultDecodeByteS = new ArrayList<>();

    int escape;
    boolean hasEscape = false;

    public void readMsg(byte[] arr, int beginPos){
        ArrayList<Integer> bits = new ArrayList<>();
        for(int i = beginPos + 1; i < arr.length; i++){
            String byte1 = Integer.toBinaryString(Byte.toUnsignedInt(arr[i]));
            char[] ch = getFull8bitByte(byte1).toCharArray();
            for(int k = 0; k < ch.length;  k++){
                bits.add(Integer.valueOf(String.valueOf(ch[k])));
            }
        }

        int countBits = 0;
        Map<Byte, String> map = new HashMap<>();

        for(int i = 0; i < bits.size(); i++){
//            if(i <= 25 && i > 20){
//                System.out.println("here");
//            }
            int bit = bits.get(i);
            if(countBits == 0) {
                decodeTree.entrySet().stream().filter(entry -> entry.getValue().startsWith(String.valueOf(bit))).forEach(entry -> {
                    map.put(entry.getKey(), entry.getValue());
                });
                if(hasEscape) {
                    if (map.containsKey(escape)) {
                        map.remove(escape);
                    }
                }
                countBits++;

            }else {

                final int t = countBits;
//                set2 = map.entrySet().stream().filter(entry -> {
//                    if(entry.getValue().length()-1 == t && entry.getValue().substring(t,t+1).equals(String.valueOf(bit))){
//                        return true;
//                    }else return false;
//
//                }).collect(Collectors.toCollection(HashSet :: new));
//                if (set2.isEmpty()){
                HashSet<Map.Entry<Byte, String>> set1 = map.entrySet().stream().filter((entry) -> {
                    if(entry.getValue().length() > t){
                        return !entry.getValue().substring(t, t + 1).equals(String.valueOf(bit));
                    }else return true;
                }).collect(Collectors.toCollection(HashSet :: new));

                set1.stream().forEach(e -> map.remove(e.getKey()));
//                }else {
//                    map.clear();
//                    set2.stream().forEach(e -> map.put(e.getKey(), e.getValue()));
//                }
//                if(map.size() == 0){
//                    set1.stream().min().forEach(e -> map.remove(e.getKey()));
//                }
                if(map.size() == 0){
                    escape = set1.stream().filter( e ->  e.getValue().length()-1 >= t).findFirst().orElse(null).getKey();
                    hasEscape = true;
//                    int rollBackSyms = map.get(escape).length();
                    i = i - (countBits);
                    countBits = -1;
                }
                if(map.size() == 1){
//                    final int[] willRead = {0};
                    map.entrySet().stream().forEachOrdered((entry) -> {
                        resultDecodeByteS.add(entry.getKey());
//                        if(entry.getValue().length() == t)
//                            willRead[0] = entry.getValue().substring(t+1).length();
                    });
                    if(map.get(resultDecodeByteS.get(resultDecodeByteS.size()-1)).length()-1 == t){
                        map.clear();
                        countBits = -1;
                        hasEscape = false;
                    }else resultDecodeByteS.remove(resultDecodeByteS.size()-1);

                }

                countBits++;
            }
        }


        resultDecodeByteS.size();

    }










}
