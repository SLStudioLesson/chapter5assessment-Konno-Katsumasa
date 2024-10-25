package com.taskapp.logic;

import com.taskapp.dataaccess.LogDataAccess;
import com.taskapp.dataaccess.TaskDataAccess;
import com.taskapp.dataaccess.UserDataAccess;
import com.taskapp.exception.AppException;
import com.taskapp.model.User;
import com.taskapp.model.Log;
import com.taskapp.model.Task;
import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;


public class TaskLogic {
    private final TaskDataAccess taskDataAccess;
    private final LogDataAccess logDataAccess;
    private final UserDataAccess userDataAccess;


    public TaskLogic() {
        taskDataAccess = new TaskDataAccess();
        logDataAccess = new LogDataAccess();
        userDataAccess = new UserDataAccess();
    }

    /**
     * 自動採点用に必要なコンストラクタのため、皆さんはこのコンストラクタを利用・削除はしないでください
     * @param taskDataAccess
     * @param logDataAccess
     * @param userDataAccess
     */
    public TaskLogic(TaskDataAccess taskDataAccess, LogDataAccess logDataAccess, UserDataAccess userDataAccess) {
        this.taskDataAccess = taskDataAccess;
        this.logDataAccess = logDataAccess;
        this.userDataAccess = userDataAccess;
    }

    /**
     * 全てのタスクを表示します。
     *
     * @see com.taskapp.dataaccess.TaskDataAccess#findAll()
     * @param loginUser ログインユーザー
     */
    //statusとuser_codeは文字列に変換する
    public void showAll(User loginUser) {
        List<Task> tasks = taskDataAccess.findAll();

        tasks.forEach(task -> {
            String status = "未着手";
            // String rUserCode ="あなたが担当しています";
            String rUserCode ="";
            
            if ((task.getStatus() == 1)) {
                status = "着手中";
            }else if ((task.getStatus() == 2)) {
                status = "完了";
            }
            
            if ((task.getRepUser().getCode() == loginUser.getCode())) {
                rUserCode ="あなたが担当しています";
            }else{
                rUserCode = task.getRepUser().getName()+"が担当しています";
            }
            
            //int code, String name, int status, User repUser
            System.out.println(task.getCode()+". タスク名："+task.getName()+
            ", 担当者名："+rUserCode+", ステータス："+status);
        });

        // System.out.println("以下1~2から好きな選択肢を選んでください。");
        // System.out.print("");
    }

    /**
     * 新しいタスクを保存します。
     *
     * @see com.taskapp.dataaccess.UserDataAccess#findByCode(int)
     * @see com.taskapp.dataaccess.TaskDataAccess#save(com.taskapp.model.Task)
     * @see com.taskapp.dataaccess.LogDataAccess#save(com.taskapp.model.Log)
     * @param code タスクコード
     * @param name タスク名
     * @param repUserCode 担当ユーザーコード
     * @param loginUser ログインユーザー
     * @throws AppException ユーザーコードが存在しない場合にスローされます
     */
    public void save(int code, String name, int repUserCode,
                    User loginUser) throws AppException {
            User user = userDataAccess.findByCode(repUserCode);
            if (user == null) {
                throw new AppException("存在するユーザーコードを入力してください");
            }
            Task task = new Task(code, name, repUserCode, user);
            taskDataAccess.save(task);
            //Task_Code,Change_User_Code,Status,Change_Date
            //logDataAccess
            //データは受け取ってるのでここでLogに渡す
            //Change_Userはログインしてるユーザーのユーザーコード
            int logChangeUcode = loginUser.getCode();
            Log logs = new Log(code, logChangeUcode, 0, LocalDate.now());
            //データアクセスに渡す
            logDataAccess.save(logs);
}

    /**
     * タスクのステータスを変更します。
     *
     * @see com.taskapp.dataaccess.TaskDataAccess#findByCode(int)
     * @see com.taskapp.dataaccess.TaskDataAccess#update(com.taskapp.model.Task)
     * @see com.taskapp.dataaccess.LogDataAccess#save(com.taskapp.model.Log)
     * @param code タスクコード
     * @param status 新しいステータス
     * @param loginUser ログインユーザー
     * @throws AppException タスクコードが存在しない、またはステータスが前のステータスより1つ先でない場合にスローされます
     */
    public void changeStatus(int code, int status,
                            User loginUser) throws AppException {
                    
            Task task = taskDataAccess.findByCode(code);//ユーザーが選択したコードに対応したcsvのデータを受け取った
            //この時点ではnullの可能性がある。
            if ((task == null)) {
                throw new AppException("存在するタスクコードを入力してください");
            }
            int taskCode = task.getStatus();
            if (taskCode ==0&& status ==2) {//変更できるパターン 0->1,1->2
                throw new AppException("ステータスは、前のステータスより1つ先のもののみを選択してください");
            }else if((taskCode == 2)){
                throw new AppException("ステータスは、前のステータスより1つ先のもののみを選択してください");
            }else if (taskCode == 1 && status == 1) {
                throw new AppException("ステータスは、前のステータスより1つ先のもののみを選択してください");
            }
            task = new Task(task.getCode(), task.getName(), status, loginUser);
            taskDataAccess.update(task);//この内容を書き込む
            //int taskCode, int changeUserCode, int status, LocalDate changeDate
            //Logにもデータを渡すStatusは変更後のステータスChange_User_Codeは今ログインしてるユーザーコード
            //taskCodeはそのままcode,chengeUsercodeはloginUser.getcode,sttusは受け取った方のstatus,
            Log log = new Log(code, loginUser.getCode(), status, LocalDate.now());
            //logをlogDataAccessに渡して書き込ませる
            logDataAccess.save(log);

    }

    /**
     * タスクを削除します。
     *
     * @see com.taskapp.dataaccess.TaskDataAccess#findByCode(int)
     * @see com.taskapp.dataaccess.TaskDataAccess#delete(int)
     * @see com.taskapp.dataaccess.LogDataAccess#deleteByTaskCode(int)
     * @param code タスクコード
     * @throws AppException タスクコードが存在しない、またはタスクのステータスが完了でない場合にスローされます
     */
    // public void delete(int code) throws AppException {
    // }
}