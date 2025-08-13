package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
//追加パッケージ
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();

		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		File[] files = new File(args[0]).listFiles();
		List<File> rcdFiles =new ArrayList<>();

		//ファイルの数繰り返し処理
		for (int i = 0;i < files.length; i++) {

			//files[i].getnName();//ファイル名取得　この文字から始まる記号がいる　この文字で終わる　.は正規表現で別の意味
			if(files[i].getName().matches("^[0-9]{8}\\.rcd$")){
				rcdFiles.add(files[i]);

			}
			//売上ファイルのリストのソート
			Collections.sort(rcdFiles);
			
			//売上ファイルが連番になっている確認 繰り返し回数は売上ファイルのリストの数より１つ小さくなるのは次のファイルと比べるため少なくしないと最後の比較で別のエラーがでる。
			for(int j = 0;j < rcdFiles.size() -1;j++) {
				//比較する２つのファイルの先頭から数字の８文字を切り出しint型に
				int former = Integer.parseInt(rcdFiles.get(j).getName().substring(0,8));
				int latter = Integer.parseInt(rcdFiles.get(j + 1).getName().substring(0,8));
				
				//2つのファイルの差が１でない場合エラー処理
				if((latter - former) != 1) {
					System.out.println("売上ファイル名が連番になっていません");
					return;
				}
			}
		}
		// 売上ファイル読み込み処理
		for (int i = 0;i< rcdFiles.size(); i++) {
			BufferedReader br = null;

			try {
				//rcdFilesにもファイル名あり　デバックで確認
				File file = new File(args[0], rcdFiles.get(i).getName());
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				//空文字で初期化
					String line = "";

				//売上ファイル支店コード、売れ上げ金額のリスト
				List<String> items = new ArrayList<>();

				// 一行ずつ読みこむ
				while((line = br.readLine()) != null) {
					items.add(line);

				}
				//支店コードが定義ファイルにあるか確認
				if (!branchNames.containsKey(items.get(0))) {
					System.out.println("<" +  rcdFiles.get(i).getName() + ">の支店コードが不正です");
					return;
				}

				long fileSale = Long.parseLong(items.get(1));

				//読み込んだ売上金額を加算
				Long saleAmount = branchSales.get(items.get(0)) + fileSale;
				
				//売上金額１０桁以上の確認
				if(saleAmount >= 10000000000L) {
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				//mapに加算した値を追加
				branchSales.put( items.get(0), saleAmount);


			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return ;
			} finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}
		//3-1支店別集計ファイル書き込み
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT , branchNames, branchSales)) {
			return;
		}
	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			 //支店定義ファイルが存在しない場合、コンソールにエラーメッセージを表⽰します。 
			if(!file.exists()) { 
				System.out.println("支店定義ファイルが存在しません");
				return false;
			   
			} 
			//File file = new File("C:\\Users\\trainee1206\\Desktop\\売上集計課題\\branch.lst","branch.lst");
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			// line変数空文字初期化
			String line = "";

			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] items = line.split(",");
				
				//支店定義ファイルのフォーマット確認
				if((items.length != 2) || (!items[0].matches("^[0-9]{3}$"))){  
				    //支店定義ファイルの仕様が満たされていない場合エラメッセージ表示
				    System.out.println("支店定義ファイルのフォーマットが不正です。");
				    return false;
				}

				//Mapに追加する２つの情報をputの引数として指定
				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L);

			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;

		try {
			File file = new File(path, fileName);

			//ファイルライターの変数fw
			FileWriter fw = new FileWriter(file);

			//バッファードライターの変数bw
			bw = new BufferedWriter(fw);




			// 一行ずつ書き込む支店コード、支店名、売上金額
			for(String key : branchNames.keySet()) {
				bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
				bw.newLine();


			}


		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

}