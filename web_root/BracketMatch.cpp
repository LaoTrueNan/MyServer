#include <Stack.h>
#include <string.h>

//利用栈完成括号匹配功能
//输入一个字符串，对其中的左右括号进行匹配
void BracketMatch(char *target){
     Stack<int> s;
     int length = strlen(target);
     int i=0;
     for(int j =1;j<=length;j++){
         if(target[j-1]=='('){
             s.push(j);
         }else if(target[j-1]==')'){
             if(s.pop(i)){
                 cout<<i<<" and "<<j<<" matched."<<endl;
             }else{
                 cout<<"No left bracket matches with "<<j<<endl;
             }
         }
     }
     while(!s.isEmpty()){
         s.pop(i);
         cout<<"No right brackets matches with "<<i<<endl;
     }
}

int main(){
    int i;
    cin>>i;
    char *a = new char[i];
    for(int j=0;j<i;j++){
        cin>>a[j];
    }
    BracketMatch(a);
    return 0;
}