import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Evgenia on 19.11.2016.
 */
public class Compression {
    public static int COMPRESS_MODE = 0;
    public static int DECOMPRESS_MODE = 1;

    private int selectedMode = 0;
    private String srcFilePath;
    private String dstFilePath;

    private Map<Byte[], Double> byteFreqMap = new HashMap<>();

    private Map<Byte, Double> byteFreqMapConst = new HashMap<>();

    private Map<Byte, ArrayList<Integer>> resultBytesCode = new HashMap<>();


    public Compression(int mode, String srcFilePath, String dstFilePath){
        selectedMode = mode;
        this.srcFilePath = srcFilePath;
        this.dstFilePath = dstFilePath;

        if (mode == COMPRESS_MODE){
            compressFile();
        }else {
            decompressFile();
        }
    }

    public void compressFile(){
        byte[] receivedArr = readFromFile();
        countDelRepeats(receivedArr);
        findCode();
        codeFile(receivedArr);

    }

    public void decompressFile(){
        byte[] receivedArr = readFromFile();
        decodeFile(receivedArr);
    }

    /*============================ CODING FILE ===========================*/
    private byte[] readFromFile(){

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        InputStream input = null;
        try {
            input = new BufferedInputStream(new FileInputStream(srcFilePath));
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

    private void countDelRepeats(byte[] byteArr){

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

    private void generateResultArr(ArrayList<Byte> byteArr){
        for(int i = 0; i < byteArr.size(); i++){
            ArrayList<Integer> resCode = new ArrayList<>();
            resultBytesCode.put(byteArr.get(i), resCode);
        }
    }

    private void findCode(){
        while (byteFreqMap.entrySet().size() > 1){
            findTwoMinimals(byteFreqMap);
        }
    }

    private void findTwoMinimals(Map<Byte[], Double> map){
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

    private Byte[] concateArrs(Byte[] bar1, Byte[] bar2){
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

    private void splitIntoBytesAndPut(Byte[] bar, int num){
        for(int i = 0; i < bar.length; i++){
            if(resultBytesCode.containsKey(bar[i])){
                resultBytesCode.get(bar[i]).add(0, num);
            }else System.out.println("not contain byte " + bar[i]);
        }

    }


/*записываем в файл закодированное сообщение:
* сначала пишем длину закодированноего дерева как байтовое число
* потом записываем само закодированное дерево
* записываем закодированную строку*/
    private void codeFile(byte[] codeString){
        printTree(resultBytesCode, "codeTree.txt");

        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < codeString.length; i++){
            if(resultBytesCode.containsKey(codeString[i])){
                sb.append(resultBytesCode.get(codeString[i]).toString().replaceAll("[,\\]\\[\\s]*", ""));
            }else{
                System.out.println("not contain byte " + codeString[i]);
            }

        }
        System.out.println(srcFilePath + " : " + sb.toString().length());


        DataOutputStream os = null;
        try {
            os = new DataOutputStream(new FileOutputStream(dstFilePath));
            byte[] b = codeKeysMapToBytes(resultBytesCode);
            int size = b.length;

//            int countSizes = 0;
//            if(size > 255){
//                countSizes = (int)Math.ceil(size/255.0);
//            }
//            os.write(((Integer)countSizes).byteValue());
//            os.write();
            String sizeS = Integer.toBinaryString(size-1);
            int countMainBits = sizeS.length();
            byte[] sizeB = getBits(sizeS);
            os.write(((Integer)countMainBits).byteValue());
            os.write(sizeB);
            os.write(b);
            System.out.println("head size : " + (size + 1 + sizeB.length));
            os.write(getBits(sb.toString()));
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void printTree (Map<Byte, ArrayList<Integer>> map, String fileName){
        DataOutputStream os = null;
        try {
            os = new DataOutputStream(new FileOutputStream("C:\\Users\\Evgenia\\IdeaProjects\\OTIKLab2\\Texts\\" + fileName));
            DataOutputStream finalOs = os;
            map.entrySet().stream().forEach(e -> {
                try {
                    finalOs.writeBytes(e.getKey() + "--->" + e.getValue() + "\n");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
            os.close();
        }catch (FileNotFoundException fe){
            fe.printStackTrace();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    /*разбивает полученную строку закодированных символов на байты
    * если число байтов получается не целым, дополняет конец строки нулями*/
    private byte[] getBits(String str) {
        String s = "00000000";
        int strSize = str.length();
        int byteArrSize = Math.max((int) Math.ceil((double) strSize / 8.0),1);
        int remainder = strSize % 8;
        if(remainder != 0) str = str + s.substring(0, 8 - remainder);
        byte[] b = new byte[byteArrSize];
        int count = 0;

        /*делим полученную строку на байты по 8 цифр,
        переводим полученный двоичный код в целое число,
        и записываем его как байтовое значение
        тк мы делим строку ровно по 8 бит, то у нас не может получиться числа больше 255*/
        for (int i = 0; i < str.length(); i += 8) {
            int val = Integer.parseInt(str.substring(i, i + 8), 2);
            b[count++] = ((Integer) val).byteValue();

        }
        return b;
    }

    /*кодирует дерево, чтобы записать его в файл ввиде байтовой строки
    * пишем символ, коорый кодируем
    * пишем число 0 и 1 которое его кодирует
    * записываем код символа в двоичном виде, дополняя его до нужного количества байто если необходимо*/
    public byte[] codeKeysMapToBytes( Map<Byte, ArrayList<Integer>> map){
        ArrayList<Byte> b = new ArrayList<>();
        for(Map.Entry<Byte, ArrayList<Integer>> entry : map.entrySet()){
            b.add(entry.getKey());
            b.add(((Integer)entry.getValue().size()).byteValue());
            byte[] bytes1 = getBits(entry.getValue().toString().replaceAll("[,\\]\\[\\s]*",""));
            for(int i = 0; i < bytes1.length; i++){
                b.add(bytes1[i]);
            }
        }
        byte[] bytes = new byte[b.size()];
        for(int i = 0; i < b.size(); i++){
            bytes[i] = b.get(i);
        }
        return bytes;
    }



    /*============================ DECODING FILE ===========================*/

    Map<Byte, String> decodeTree = new HashMap<>();

    /*декодируем файл
    * считываем первый байт - размер закодированного дерева
    *
    * */
    public void decodeFile(byte[] fileByteArr){
        int countMainBits = Byte.toUnsignedInt(fileByteArr[0]);

        int t = (int)Math.ceil(countMainBits/8.0);

        StringBuilder sb1 = new StringBuilder();
        for(int k = 1; k <= t; k++){
            sb1.append(getFull8bitByte(Integer.toBinaryString(Byte.toUnsignedInt(fileByteArr[k]))));
        }
        String s3 = sb1.toString();
        String s4 = s3.substring(0, countMainBits);
        int treeSize = Integer.parseInt(s4, 2);



        for(int i = t+1; i < treeSize+t+2; i+=3){
            // считываем число 0 и 1 которыми закодировли символ
            int bits = Byte.toUnsignedInt(fileByteArr[i+1]);

            String s1;
            String s2;
            int g = 1;
            if(bits > 8){
                g = (int)Math.ceil(bits/8.0);
                StringBuilder sb = new StringBuilder();
                for(int k = 0; k < g; k++){
                    sb.append(getFull8bitByte(Integer.toBinaryString(Byte.toUnsignedInt(fileByteArr[i+2+k]))));
                }
                s1 = sb.toString();
                s2 = s1.substring(0, bits);
            }else {
                s1 = Integer.toBinaryString(Byte.toUnsignedInt(fileByteArr[i+2]));
                s2 = getFull8bitByte(s1).substring(0, bits);
            }
            // считываем код символа в виде одного байта
            decodeTree.put(fileByteArr[i], s2);
            i+=g-1;
        }
        decodeTree.size();

        DataOutputStream os = null;
        try {
            os = new DataOutputStream(new FileOutputStream("C:\\Users\\Evgenia\\IdeaProjects\\OTIKLab2\\Texts\\decodeTree"));
            DataOutputStream finalOs = os;
            decodeTree.entrySet().stream().forEach(e -> {
                try {
                    finalOs.writeBytes(e.getKey() + "--->" + e.getValue() + "\n");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
            os.close();
        }catch (FileNotFoundException fe){
            fe.printStackTrace();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }

        readMsg(fileByteArr, treeSize+t+2);
        System.out.println(treeSize+t+2);
    }


    /*так как при трансформаци двоичного кода символа, нули стоящие в начале отбрасываются,
     то что бы их получить, мы их добавляем*/
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

/**/
    public void readMsg(byte[] arr, int beginPos){
        ArrayList<Integer> bits = new ArrayList<>();
        for(int i = beginPos; i < arr.length; i++){
            String byte1 = Integer.toBinaryString(Byte.toUnsignedInt(arr[i]));
            char[] ch = getFull8bitByte(byte1).toCharArray();
            for(int k = 0; k < ch.length;  k++){
                bits.add(Integer.valueOf(String.valueOf(ch[k])));
            }
        }
/*
* */
        int countBits = 0;
        Map<Byte, String> map = new HashMap<>(); // группа кодов, начальный символ которых совпадает с первым битом нового слова

        for(int i = 0; i < bits.size(); i++){
//            if(i <= 25 && i > 20){
//                System.out.println("here");
//            }
            int bit = bits.get(i);
            if(countBits == 0) {
                decodeTree.entrySet().stream().filter(entry -> entry.getValue().startsWith(String.valueOf(bit))).forEach(entry -> {
                    map.put(entry.getKey(), entry.getValue());
                });
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
                if(map.size() == 1){
                    map.entrySet().stream().forEachOrdered((entry) -> {
                        resultDecodeByteS.add(entry.getKey());
//                        if(entry.getValue().length() == t)
//                            willRead[0] = entry.getValue().substring(t+1).length();
                    });
                    if(map.get(resultDecodeByteS.get(resultDecodeByteS.size()-1)).length()-1 == t){
                        map.clear();
                        countBits = -1;
                    }else resultDecodeByteS.remove(resultDecodeByteS.size()-1);

                }

                countBits++;
            }
        }
        resultDecodeByteS.size();
        byte[] bbb= new byte[resultDecodeByteS.size()];

        for(int i = 0; i < resultDecodeByteS.size(); i++){
            bbb[i] = resultDecodeByteS.get(i);
        }

        DataOutputStream os = null;
        try {
            os = new DataOutputStream(new FileOutputStream(dstFilePath));
            byte[] b = codeKeysMapToBytes(resultBytesCode);
            int size = b.length;
            os.write(bbb);
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
