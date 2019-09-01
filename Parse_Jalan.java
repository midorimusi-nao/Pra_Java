import java.io.*;
import java.util.*;
import java.text.Normalizer;

import java.util.stream.Collectors;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Parse_Jalan {

    public static void main(String[] args) throws IOException {

        //実行時間計測用
        long start = System.currentTimeMillis();
        Parse_Jalan PJ = new Parse_Jalan();
        //String name1 = "Jalan_Url";
        String name1 = "Jalan_City_Url";
        //String name2 ="Town_Name_Jalan";
        String name2 ="Town_City";

        String[] Filename = new String[185];
        String[] Jalan_URL = new String[185];
        PJ.FileRead(Filename,name2);
        PJ.FileRead(Jalan_URL,name1);
        ////Filename[0]="室蘭市";
        //Jalan_URL[0]="https://www.jalan.net/kankou/cit_012050000/";
        //String FileName = "jalan";

        //取得したいURL
        //String url = "https://www.jalan.net/kankou/cit_015120000/?rootCd=7741&screenId=OUW2201&influxKbn=0";
        //ユーザエージェント(ある程度最新のブラウザを指定しないとコンテンツを返してくれないサイトがある)
        //String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36";
        String userAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:24.0) Gecko/20100101 Firefox/24.0 Chrome/59.0.3071.115";

        //ファイルへの書き込み
        //FileWriter fw = new FileWriter("jaran-test-hamatonbetsu.txt");
        //PrintWriter pw = new PrintWriter(new BufferedWriter(Me));
        
        try{
            for(int i=0; i<Filename.length;i++){
                if(Filename[i] == null){
                    continue;
                }
                String url=Jalan_URL[i];
                Document document = Jsoup.connect(url).userAgent(userAgent).get();
                Elements element = document.select("a[href]");
                String Same="";
                
                while(true){
                    Same = url;
                    Kankou_URL(Filename[i],url,userAgent);
                    url = Next_URL(url,userAgent);
                    Rest();
                    if(url.equals(Same))break;
                }
                System.out.println("一つの町完了"+Filename[i]);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        //標準出力
        //System.out.format("タイトル=%1$s, 本文=%2$s", document.title(), builder.toString());

        //実行時間計測用
        long end = System.currentTimeMillis();
        System.out.println((end - start) + "ms");
        System.out.println((end - start)/1000 + "秒");
        
    }

    
    public static void Kankou_URL(String FileName,String UR,String Agent) throws IOException{
        try{
            Document document = Jsoup.connect(UR).userAgent(Agent).get();
            Elements element = document.select("a[href]");
            String kuchikomi="";
            String re_URL="";
            int i=0;
            for(Element ele : element){
                if(ele == null){
                    continue;
                }
                String str = ele.toString();
                String str2 = ele.attr("href");
                
                if(str.contains("//www.jalan.net/kankou/spt_") && str.contains("kuchikomi")){
                    i++;
                    //System.out.println("https:"+str2+"\t"+i);
                    re_URL="https:"+str2.replaceAll(" ","");
                    Reload_Kuchikomi(FileName,re_URL,Agent);
                    if(i>29)break;
                }
            }
        } catch (IOException ex) {
            String err = "Er";
            ex.printStackTrace();
        }
    }

   
    public static String Next_URL(String UR, String Agent){
        try{
            Document document = Jsoup.connect(UR).userAgent(Agent).get();
            Elements element = document.select("a[href]");
            String re_URL="";
            for(Element ele : element){
                if(ele == null){
                    continue;
                }
                String str = ele.toString();
                String str2 = ele.attr("href");
                
                if(str.contains("次の30件を表示") && str.contains("onclick")){
                    //System.out.println(str2);
                    re_URL=str2.replaceAll(" ","");
                    break;
                }else if(str.contains("kuchikomi") && str.contains("次の10件を表示") && str.contains("onclick")){
                    re_URL="https:"+str2.replaceAll(" ","");
                    //System.out.println("次の10件を表示" );
                    break;
                }else{
                    re_URL=UR;
                }
            }
            return re_URL;
        }catch(IOException e){
            String Err = "Error";
            return Err;
        }
    }

    public static void Reload_Kuchikomi(String FileName,String UR,String Agent){
        try{
            Document document = Jsoup.connect(UR).userAgent(Agent).get();
            Elements element = document.select("a[href]");
            String URL=UR;
            String Same="";
            URL = UR;

            while(true){
                Same = URL;
                Review_Input(FileName,URL,Agent);
                URL = Next_URL(URL,Agent);
                Rest();
                if(URL.equals(Same))break;
                System.out.println("次の10件を表示\t"+URL );
            }
            System.out.println("レビュー終わり");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void Review_Input(String FileName,String UR,String Agent) throws IOException{
        try{
            File file = new File("Jalan/Town_City/"+FileName+".txt");
            FileWriter fw;
            if(file.exists()){
                fw = new FileWriter("Jalan/Town_City/"+FileName+".txt",true);
            }else{
                fw = new FileWriter("Jalan/Town_City/"+FileName+".txt");
            }
            PrintWriter pw;
            pw = new PrintWriter(new BufferedWriter(fw));
            Document document = Jsoup.connect(UR).userAgent(Agent).get();
            Elements elements = document.select("div.item-reviewTextInner.item-reviewLabel");
            Elements eles =  elements.select("span");
            StringBuilder builder = new StringBuilder();
            
            for (Element element : eles){
                if (element.ownText() == null){
                    continue;
                }
                //ファイルに書き込まないで追加していく
                String str = element.ownText();
                Normalizer.normalize(str,Normalizer.Form.NFKC);
                if(StringUtils.isEmpty(str) != true){
                    pw.write(str);
                }
            }
            //System.out.println(builder.toString());
            //System.out.println(document.html());
            
            pw.flush();
            pw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

     public void FileRead(String FileName[],String Name) throws IOException{
        int i = 0;
        try{
            File file = new File("Jalan/"+Name+".txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String str= null;
            while((str = br.readLine()) != null){
                System.out.println("じゃらん:"+str);
                FileName[i]=str;
                i+=1;
           }
            br.close();
        }catch(IOException e){
           System.out.println(e);
        }
    }

    public static void Rest() throws IOException{
        try{
            Thread.sleep(4000);
        }catch(InterruptedException e){
        }
    }

    public static String readAll(final String path) throws IOException{
        return Files.lines(Paths.get(path), Charset.forName("UTF-8")).collect(Collectors.joining(System.getProperty("line.separator")));
    }

}
