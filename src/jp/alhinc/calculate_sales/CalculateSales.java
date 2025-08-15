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

	//商品定義ファイル名
	private static final String FilE_NAME_COMMODITY_LIST = "commodity.list";

	//商品定義集計ファイル名
	private static final String FILE_NAME_COMMODITY_OUT = "commodity.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	//追加のエラーメッセージ
	private static final String FILE_NOT_SERIAL_NUMBER = "売上ファイル名が連番になっていません";
	private static final String SALES_AMOUNT_OVER_TEN_DIGITS = "合計金額が10桁を超えました";
	private static final String SALES_FILE_INVALID_BRNCH_CODE = "の支店コードが不正です";
	//商品定義ファイルの追加エラーメッセージ;
	private static final String COMMODITY_FILE_INVALID_COMMODITY_CODE = "の商品コードが不正です";
	/** 変更エラーメッセージ
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String COMMODITY_FILE_NOT_EXIST = "商品定義ファイルが存在しません"
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";
	private static final String COMMODITY_FILE_INVALID_FORMAT = "商品定義ファイルのフォーマットが不正です";
	*/
	private static final String FILE_NOT_EXIST = "が存在しません";
	private static final String FILE_INVALID_FORMAT = "のフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		//コマンドライン引数が渡されているか確認
		if(args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}
		//ファイル名　支店定義ファイル
		String officeFileName = "支店定義ファイル";
		//ファイル名　商品定義ファイル
		String commodityFileName = "商品定義ファイル";
		//支店コードの形式
		String officeCode = "^[0-9]{3}$";
		//商品コードの形式
		String commodityCode = "[0-9a-zA-Z]{8}$";

		// 支店コードと支店名を保持するMap
		Map<String, String> officeNames = new HashMap<>();

		// 支店コードと売上金額を保持するMap
		Map<String, Long> officeSales = new HashMap<>();

		//商品コードと商品名を保持するMap
		Map<String, String> commodityNames = new HashMap<>();

		//商品コードと売上金額を保持するMap
		Map<String, Long> commoditySales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, officeFileName, officeCode, officeNames, officeSales)) {
			return;
		}

		//商品定義ファイル読み込み処理
		if(!readFile(args[0], FilE_NAME_COMMODITY_LIST, commodityFileName, commodityCode, commodityNames, commoditySales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		File[] files = new File(args[0]).listFiles();
		List<File> rcdFiles = new ArrayList<>();

		//ファイルの数繰り返し処理 ファイルかディレクトリかの判定も　エラー処理３
		for (int i = 0; i < files.length; i++) {

			//files[i].getnName();//ファイル名取得　この文字から始まる記号がいる　この文字で終わる　.は正規表現で別の意味
			if(files[i].isFile() && files[i].getName().matches("^[0-9]{8}\\.rcd$")){
				rcdFiles.add(files[i]);

			}
		}
		//売上ファイルのリストのソート
		Collections.sort(rcdFiles);

		//売上ファイルが連番になっている確認 繰り返し回数は売上ファイルのリストの数より１つ小さくなるのは次のファイルと比べるため少なくしないと最後の比較で別のエラーがでる。
		for(int i = 0; i < rcdFiles.size() - 1; i++) {
			//比較する２つのファイルの先頭から数字の８文字を切り出しint型に
			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

			//2つのファイルの差が１でない場合エラー処理
			if((latter - former) != 1) {
				System.out.println(FILE_NOT_SERIAL_NUMBER);
				return;
			}
		}
		// 売上ファイル読み込み処理
		for (int i = 0; i < rcdFiles.size(); i++) {
			BufferedReader br = null;

			try {
				//rcdFilesにもファイル名あり　デバックで確認
				File file = new File(args[0], rcdFiles.get(i).getName());
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				//空文字で初期化 無駄なインデントあり
				String line = "";

				//売上ファイル支店コード、売り上げ金額のリスト
				List<String> items = new ArrayList<>();

				// 一行ずつ読みこむ
				while((line = br.readLine()) != null) {
					items.add(line);

				}
				//売上ファイルのフォーマットの確認 商品定義ファイルの影響で１追加
				if(items.size() != 3) {
					System.out.println(rcdFiles.get(i).getName() + FILE_INVALID_FORMAT);
					return;
				}
				//支店コードが定義ファイルにあるか確認
				if (!officeNames.containsKey(items.get(0))) {
					System.out.println(rcdFiles.get(i).getName() + SALES_FILE_INVALID_BRNCH_CODE);
					return;
				}
				//商品コードが定儀ファイルにあるか確認
				if (!commodityNames.containsKey(items.get(1))) {
					System.out.println(rcdFiles.get(i).getName() + COMMODITY_FILE_INVALID_COMMODITY_CODE);
					return;
				}

				//売上金額が数字なのか確認売上金額Longに変換する前にエラー処理で確認
				if(!items.get(2).matches("^[0-9]+$")) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				long fileSale = Long.parseLong(items.get(2));

				//読み込んだ売上金額を加算
				Long saleAmount = officeSales.get(items.get(0)) + fileSale;

				//商品ごとに売上金額加算
				Long commodityAmount = commoditySales.get(items.get(1)) + fileSale;


				//売上金額１０桁以上の確認　支店ごと商品ごと
				if(saleAmount >= 10000000000L || commodityAmount >= 10000000000L) {
					System.out.println(SALES_AMOUNT_OVER_TEN_DIGITS);
					return;
				}

				//mapに加算した値を追加
				officeSales.put(items.get(0), saleAmount);

				//mapに加算した値を追加
				commoditySales.put(items.get(1), commodityAmount);


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
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, officeNames, officeSales)) {
			return;
		}
		//商品別集計ファイルの書き込み処理
		if(!writeFile(args[0], FILE_NAME_COMMODITY_OUT, commodityNames, commoditySales)) {
			return;
		}
	}

	/**
	 * 支店定義ファイル読み込み処理 商品定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param コード形式
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, String fileFormat, String code, Map<String, String> eachNames, Map<String, Long> eachSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			 //支店定義ファイルまたは商品定義ファイルが存在しない場合、コンソールにエラーメッセージを表示します。
			if(!file.exists()) {
				System.out.println(fileFormat + FILE_NOT_EXIST);
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
				//書きエラー処理コードを１つに
				if((items.length != 2) || (!items[0].matches(code))) {
				    //支店定義ファイルの仕様が満たされていない場合エラメッセージ表示
				    System.out.println(fileFormat + FILE_INVALID_FORMAT);
				    return false;
				}
				//Mapに追加する２つの情報をputの引数として指定
				eachNames.put(items[0], items[1]);
				eachSales.put(items[0], 0L);

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