package GUI.GUIController;

import Controller.UserController;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import model.Award;
import model.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class StudentPageController implements Initializable {

    @FXML
    private Text username;

    @FXML
    private Text grade;

    @FXML
    private Text admin;

    @FXML
    private TableView<ScoreInformation> scoretable;

    @FXML
    private TableColumn<ScoreInformation, String> subject;

    @FXML
    private TableColumn<ScoreInformation, Integer> score;

    @FXML
    private TableColumn<ScoreInformation, Integer> rank;

    @FXML
    private TableView<AwardInformation> awardtable;

    @FXML
    private TableColumn<AwardInformation, String> awardtitle;

    @FXML
    private TableColumn<AwardInformation, String> awardtime;

    @FXML
    private PieChart scorechart;

    @FXML
    private TextArea description;

    @FXML
    private Button submit;

    @FXML
    private Button createtxtFileButton;

    @FXML
    private Button createmdFileButton;

    @FXML
    private Button edit;

    private UserController usercontroller;

    private String userName;

    private final ObservableList<ScoreInformation> scoreData = FXCollections.observableArrayList();

    private final ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();

    private final ObservableList<AwardInformation> awardData = FXCollections.observableArrayList();

    //生成简历文件txt格式
    @FXML
    public void createtxtFile(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("请选择文件保存位置");
        File directory = null;
        while(directory == null){
            directory = chooser.showDialog(null);
        }
        File file = new File(directory+"/"+userName+"Resume.txt");
        PrintWriter outFile = null;
        try{
            outFile = new PrintWriter(file);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        model.File text = new model.File(userName);
        outFile.println(text.gettxtFile());
        outFile.close();
    }

    //生成简历文件md格式
    @FXML
    public void createmdFile(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("请选择文件保存位置");
        File directory = null;
        while(directory == null){
            directory = chooser.showDialog(null);
        }
        File file = new File(directory+"/"+userName+"Resume.md");
        PrintWriter outFile = null;
        try{
            outFile = new PrintWriter(file);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        model.File text = new model.File(userName);
        outFile.println(text.getmdFile());
        outFile.close();
    }

    //修改个人简介和获奖记录
    @FXML
    public void editdescriptionandaward(ActionEvent event) {
        String info="双击表格编辑个人简介及奖项\n输入后，按下回车确认\n完成输入后点击提交修改";
        Alert alert = new Alert(Alert.AlertType.INFORMATION, info, new ButtonType("确定", ButtonBar.ButtonData.YES));
        alert.setHeaderText(null);
        alert.setTitle("修改个人简介及奖项");
        alert.show();
        awardtable.setEditable(true);
        awardtitle.setCellFactory(TextFieldTableCell.forTableColumn());
        awardtime.setCellFactory(TextFieldTableCell.forTableColumn());
    }
    //提交个人简介和获奖记录
    @FXML
    public void submitdescriptionandaward(ActionEvent event) {
        String info="修改成功";
        Alert alert = new Alert(Alert.AlertType.INFORMATION, info, new ButtonType("确定", ButtonBar.ButtonData.YES));
        alert.setHeaderText(null);
        alert.setTitle("修改成绩");
        alert.show();
        ArrayList<Award> newAwards = new ArrayList<Award>();
        for(int i = 0; i < awardData.size(); i++){
            newAwards.add(awardData.get(i).toAward());
        }
        usercontroller.resetAwardsByUsername(userName, newAwards);
        usercontroller.setDescriptionByUsername(userName, description.getText());
    }

    //设置本页面各部分显示的内容
    public void reset(){
        try {
            Socket socket = new Socket("localhost",1056);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (userName==null) {
                userName = input.readLine();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        User user = usercontroller.getUserByUsername(userName);

        setInformation(user);
        setDescription(user);
        setScoretable(user);
        setChart(user);
        setAwardtable(user);
    }

    //设置顶部个人信息，包括姓名、班级、身份
    public void setInformation(User user){
        username.setText("姓名："+user.getUsername());
        grade.setText("班级："+user.getGroupID());
        if(user.getIsAdmin()==1){
            admin.setText("身份：老师");
        }
        else{
            admin.setText("身份：学生");
        }
    }

    //设置个人简介
    public void setDescription(User user){
        description.setText(user.getDescription());
    }

    //设置成绩表格
    public void setScoretable(User user) {
        List<Integer> grades = user.getGrades();
        List<Integer> ranks = user.getRanks();
        for(int i = 0; i < grades.size(); i++){
            scoreData.add(new ScoreInformation(MainApp.subjects.get(i), grades.get(i), ranks.get(i)));
        }
        subject.setCellValueFactory(scoreData->scoreData.getValue().getTsubject());
        score.setCellValueFactory(scoreData->scoreData.getValue().getTscore().asObject());
        rank.setCellValueFactory(scoreData->scoreData.getValue().getTrank().asObject());
        scoretable.setItems(scoreData);
    }

    //设置成绩分布饼图
    public void setChart(User user){
        int count[] = new int[5];
        List<Integer> grades = user.getGrades();
        for(int i = 0; i < grades.size(); i++){
            if(grades.get(i)<60){
                count[0]++;
            }
            else if(grades.get(i)<70){
                count[1]++;
            }
            else if(grades.get(i)<80){
                count[2]++;
            }
            else if(grades.get(i)<90){
                count[3]++;
            }
            else{
                count[4]++;
            }
        }

        for(int i = 0; i < 5; i++){
            if(count[i]>0){
                String interval;
                if(i<4 && i>1){
                    interval = (50+10*i) +"-"+ (59+10*i);
                }
                else if(i==0){
                    interval = "<60";
                }
                else{
                    interval = "90-100";
                }
                double percentage = (double)count[i]/2*100;
                int percent = (int) percentage;
                chartData.add(new PieChart.Data(interval,percent));
            }
        }
        scorechart.setData(chartData);
        scorechart.setTitle("成绩分布表");
    }

    //设置获奖记录表格
    public void setAwardtable(User user){
        List<Award> awards = user.getAwards();
        for(int i = 0; i < 4; i++){
            if(i<awards.size()){
                awardData.add(new AwardInformation(awards.get(i).getTitle(), awards.get(i).getTime()));
            }
            else{
                awardData.add(new AwardInformation("", ""));
            }
        }

        awardtitle.setCellValueFactory(awardData->awardData.getValue().getName());
        awardtime.setCellValueFactory(awardData->awardData.getValue().getTime());
        awardtable.setItems(awardData);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usercontroller = new UserController();
    }
}

class AwardInformation {
    private final StringProperty title;
    private final StringProperty time;

    public AwardInformation(String title, String time){
        this.title = new SimpleStringProperty(title);
        this.time = new SimpleStringProperty(time);
    }

    public void setName(String title){
        this.title.set(title);
    }

    public StringProperty getName(){
        return title;
    }

    public void setTime(String time){
        this.time.set(time);
    }

    public StringProperty getTime(){
        return time;
    }

    public Award toAward(){
        return new Award(title.getValue(), time.getValue());
    }
}

class ScoreInformation {
    private final StringProperty tsubject;
    private final IntegerProperty tscore;
    private final IntegerProperty trank;

    public ScoreInformation(String subject, Integer score, Integer rank){
        tsubject = new SimpleStringProperty(subject);
        tscore = new SimpleIntegerProperty(score);
        trank = new SimpleIntegerProperty(rank);
    }

    public void setTsubject(String subject){
        tsubject.set(subject);
    }

    public StringProperty getTsubject(){
        return tsubject;
    }

    public void setTscore(int score){
        tscore.set(score);
    }

    public IntegerProperty getTscore(){
        return tscore;
    }

    public void setTrank(int rank){
        trank.set(rank);
    }

    public IntegerProperty getTrank(){
        return trank;
    }
}