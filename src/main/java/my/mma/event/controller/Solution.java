//class Solution {
//
//    static boolean[][][][][] visited = new boolean[30][30][30][30][30];
//
//    boolean isAsc(int a, int b, int c, int d, int e){
//        return a < b && b < c && c < d && d < e;
//    }
//
//    int backTrack(int a, int b, int c, int d, int e, int n, int[][]q, int[] ans){
//        if(a>n||b>n||c>n||d>n||e>n){
//            return 0;
//        }
//        if(!isAsc(a,b,c,d,e) || visited[a-1][b-1][c-1][d-1][e-1]){
//            return 0;
//        }
//        visited[a-1][b-1][c-1][d-1][e-1] = true;
//        boolean isTrue = true;
//        for(int j=0;j<q.length;j++){
//            int curr_ans = ans[j];
//            int temp = 0;
//            for(int k=0;k<5;k++){
//                if(q[j][k]==a || q[j][k]==b||q[j][k]==c||q[j][k]==d||q[j][k]==e)
//                    temp++;
//            }
//            if(temp != curr_ans){
//                isTrue = false;
//                break;
//            }
//        }
//        int res = backTrack(a+1,b,c,d,e,n,q,ans)+backTrack(a,b+1,c,d,e,n,q,ans)+
//                backTrack(a,b,c+1,d,e,n,q,ans)+backTrack(a,b,c,d+1,e,n,q,ans)+
//                backTrack(a,b,c,d,e+1,n,q,ans);
//        return isTrue ? res + 1 : res;
//    }
//
//    public int solution(int n, int[][] q, int[] ans) {
//        int answer = 0;
//        answer = backTrack(1,2,3,4,5,n,q,ans);
//        return answer;
//    }
//}