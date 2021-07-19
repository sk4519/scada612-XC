package cn.bp.scada;

import cn.bp.scada.common.constant.Matpoint;
import cn.bp.scada.common.utils.data.DateUtils;
import org.junit.Before;
import org.junit.Test;


import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

public class TestLambda {

   // @Test
    public void test1(){
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(5);
        list.add(4);

        //lambda顺序遍历list
        list.forEach(System.out::println);
        //lambda按从大到小排序遍历
       list.sort((a,b) -> a.compareTo(b.intValue()));
       System.out.println(list);
        //lambda判断是否存在某个数
        list.stream().filter(s->s > 1).forEach(System.out::println);

    }

    /**
     * 语法格式一：无参数，无返回值
     */
    //@Before
    public void test2(){
        Runnable runr = new Runnable() {
            @Override
            public void run() {
                System.out.println("new启动线程");
            }
        };
        runr.run();
        //lambda表达式启动
        Runnable lambRun = () -> System.out.println("lambda启动线程");
        lambRun.run();
    }

    /**
     * 语法格式二：有一个参数，并且无返回值
     */
    //@Before
    public void test3() {
        Consumer<String> consumer = e->System.out.println("consumer"+e);
        consumer.accept("123");
    }

    /**
     * 冒泡排序
     */

    public void test4(){
      int arr[] = new int[800000];
      for(int index =0; index < 80000; index ++) {
          arr[index] = (int) (Math.random() * 800);
      }
        System.out.println(DateUtils.getNowDate()); ;
      for (int i= 0 ; i<arr.length-1; i++) {
          for (int j = 0; j<arr.length-1-i; j++) {
              if(arr[j] > arr[j+1]) {
                  int temp = arr[j];
                  arr[j] = arr[j+1];
                  arr[j+1] = temp;
              }
          }
      }
        System.out.println(DateUtils.getNowDate());
        System.out.println(Arrays.toString(arr));
    }

    /**
     * 插入排序
     */

    public void test5() {
        int arr[] = new int[800];
        for(int index =0; index < 800; index++) {
            arr[index] = (int) (Math.random() * 80);
        }
        System.out.println(Arrays.toString(arr));
        System.out.println(DateUtils.getNowDate()); ;
        for (int i =1 ; i<arr.length; i++) {
            int j = i;
            while (j >0) {
                if(arr[j] > arr[j-1]) {
                    int temp;
                    temp = arr[j];
                    arr[j] = arr[j-1] ;
                    arr[j-1] = temp;
                    j--;
                } else {
                    break;
                }
            }
        }
        System.out.println(DateUtils.getNowDate()); ;
        System.out.println(Arrays.toString(arr));
    }

    /**
     * 插入排序优化
     */

    public void test6() {
        int arr[] = new int[700];
        for(int index =0; index < 700; index++) {
            arr[index] = (int) (Math.random() * 80);
        }
        System.out.println(Arrays.toString(arr));
        System.out.println(DateUtils.getNowDate()); ;
      for (int i=1;i<arr.length; i++) {
          int val = arr[i];
          int j;
          for ( j = i; j>0 && val < arr[j-1]; j--) {
              arr[j] = arr[j-1];
          }
          arr[j] = val;
      }
        System.out.println(DateUtils.getNowDate()); ;
        System.out.println(Arrays.toString(arr));
    }

    /**
     * 二分查找
     */
    //@Before
    public void test7() {
        int arr[] = new int[50];
        for(int index =0; index < 50; index ++) {
            arr[index] = (int) (Math.random() *5);
        }
        System.out.println(Arrays.toString(arr));
        int left = 0;
        int right = arr.length-1;
        while (left <= right) {
            int mid = (right - left) /2 ;//中间数
            if(arr[mid] == 3) {
                System.out.println("找到了，为：" + arr[mid]);
                break;
            }
            else if(arr[mid] < 3)
                left = mid +1;
            else if(arr[mid] > 3)
                right = mid -1;
        }
    }



    /**
     * 二分查找左侧边界
     */
    //@Before
    public void test8() {
        int arr[] = new int[50];
        for(int index =0; index < 50; index ++) {
            arr[index] = (int) (Math.random() *5);
        }
        System.out.println(Arrays.toString(arr));
        int left = 0;
        int right = arr.length;
        while (left < right) {
            int mid = (left + right)/2;
            if(arr[mid] == 3)
                right = mid;
            else if(arr[mid] < 3)
                left = mid +1;
            else if(arr[mid] > 3)
                right = mid;
        }
        System.out.println(left);
    }

   /* public void test9() {
        String page = "page";
        Map<String,Object> map = new HashMap<>();
       List list = new ArrayList<>();
        for (int i= 1 ;i <=7; i++) { //这样会从page1-page7
            Object obj = null;
            page = page+i;
            for(vo co:list) {

                //把参数传进去，进行7次外循环，内循环根据list大小来
                co.getUserName
                obj= redis.opsForHash.get(redisHashName,page)
                if(obj ！= null)
                switch (i) {
                    case 1 :
                        co.setdata1(1) //其他data就默认为0,
                        break;
                    case 2 :  //一直case到7
                        co.setdata2(1)
                        break;
                }

            }






        }
        map.put("list",list); //放这儿

        //将上面循环更改为lambda表达式
        Object obj = null;
        list.stream().filter(n -> ()).forEach(
        if(obj == null)
            map.put(page,0);
        else
            map.put(page,1);
        map.put("");//比如第一次循环查出的结果，放入map里
        );
        return map;
    }*/

    /**
     * 递归算法
     * @param n
     * @return
     */
   public static int f(int n) {
       if(n<1) {
           return 0;
       }
       if(n==1) {
           return 1;
       }
       if(n==2) {
           return 2;
       }
       return f(n-1) +f(n-2);
   }

    static int f1(int n) {
        // 先写递归结束条件
        if (n <= 2) {
            return 1;
        }
        // 写等价关系式
        return f1(n - 1) + f1(n - 2);
    }

    @Test
    public void test() throws Exception {
        Class<?> aClass = Class.forName("cn.bp.scada.common.constant.Matpoint");
        Matpoint o = (Matpoint) aClass.newInstance();
        final Field[] declaredFields = aClass.getDeclaredFields();
        for (Field field:declaredFields) {
            System.out.println(field.getName());
        }
    }

/*    public static void main(String[] args) {
       //斐波那契数列,递归算法
        final int i = TestLambda.f(4);
        System.out.println("跳法有"+i+"种");

        final int i1 = TestLambda.f1(6);
        System.out.println("第n项值是"+i1);
    }*/
}
