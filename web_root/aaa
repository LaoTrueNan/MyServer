#ifndef POLYNOMAL_H
#define POLYNOMAL_H
#include <iostream>
using namespace std;

struct Term{
    float coef;
    int exp;
    Term *link;
    Term(float c,int e,Term *next=NULL){coef=c;exp=e;link=next;}
    Term *insertAfter(float c,int e);
    friend ostream& operator << (ostream& out,Term& term);
};

class Polynomal{
public:
    Polynomal(){first = new Term(0,-1);}
    //copy constructor
    Polynomal(Polynomal& p);
    Term *getHead() const {
        return first;
    }
    int maxOrder();//get the biggest exp
private:
    Term *first;//head term

    friend istream& operator >> (istream&,Polynomal&);
    friend ostream& operator << (ostream&,Polynomal&);
    friend Polynomal operator + (Polynomal&,Polynomal&);
    friend Polynomal operator * (Polynomal&,Polynomal&);
};

Term* Term::insertAfter(float c,int e){
    link=new Term(c,e,link);
    return link;
};

ostream& operator << (ostream& out,Term& term){
    if(term.coef==0.0){
        return out;
    }
    out<<term.coef;
    switch(term.exp){
        case 0:break;
        case 1:out << "X";break;
        default:out<<"X^"<<term.exp;break;
    }
    return out;
};

Polynomal::Polynomal(Polynomal& p){
    first = new Term(0,-1);
    Term *destptr = first,*srcptr = p.getHead()->link;
    while(srcptr != NULL){
        destptr->insertAfter(srcptr->coef,srcptr->exp);
        destptr = destptr->link;
        srcptr = srcptr->link;
    }
    destptr->link = NULL;
};

int Polynomal::maxOrder(){
    Term *current = first;
    int max = 0;
    while(current->link != NULL){
        current = current->link;
        if(current->exp>max){max = current->exp;}
    }
    return max;
};
//将相同指数项进行合并 提高程序健壮性
istream& operator >> (istream& in,Polynomal& p){
    Term *rear = p.getHead();
    int c,e;
    //int *cnt;
    while(1){
        cout<<"Input a term(coef,exp):"<<endl;
        in>>c>>e;
        if(e<0){break;}
    rear = rear->insertAfter(c,e);
      //  cnt[e] += c;
    }
    rear->link = NULL;
    // for(int i=0;i<cnt.)
    return in;
};

ostream& operator << (ostream& out,Polynomal& p){
    Term *current = p.getHead()->link;
    int tag=0;
    while(current != NULL){
        if(tag == 1 && current->coef>0.0){
            out<<"+";
        }
        tag=1;
        out<<*current;
        current = current->link;
    }
    out<<endl;
    return out;
};

Polynomal operator +(Polynomal& op1,Polynomal& op2){
    Polynomal res;
    Term *t1,*t2,*t3,*rest;
    float temp;
    t3 = res.first;
    t1 = op1.getHead()->link;
    t2 = op2.getHead()->link;
    while(t1 != NULL && t2 != NULL){
        if(t1->exp == t2->exp){
            temp = t1->coef + t2->coef;
            if(temp>0.001||temp<-0.001){
                t3 = t3->insertAfter(temp,t1->exp);
            }
            t1 = t1->link;
            t2 = t2->link;
        }else if(t1->exp<t2->exp){
            t3 = t3->insertAfter(t1->coef,t1->exp);
            t1 = t1->link;
        }else{
            t3 = t3->insertAfter(t2->coef,t2->exp);
            t2 = t2->link;
        }
    }
    if(t1 != NULL) rest = t1;
    else rest = t2;
    while(rest != NULL){
        t3 = t3->insertAfter(rest->coef,rest->exp);
        rest = rest->link;
    }
    t3->link=NULL;
    return res;
};
/*========================
* op1的每一项乘以op2的每一项
* 系数相乘 指数相加
========================*/
Polynomal operator *(Polynomal& op1,Polynomal& op2){
    Polynomal res;
    Term *t1,*t2,*t3;
    t3 = res.first;
    t1 = op1.getHead()->link;
    int temp = 0;
    int cnt = op1.maxOrder()+op2.maxOrder();
    float *result = new float[cnt+1];
    for(int j = 0;j<cnt+1;j++){result[j] =0.0;}
    while(t1 != NULL){
        t2 = op2.getHead()->link;
        while(t2 != NULL){
            temp = t1->exp+t2->exp;
            result[temp] += t1->coef*t2->coef;
            t2 = t2->link;
        }
        t1 = t1->link;
    }
    for(int i=0;i<=cnt;i++){
        if(result[i]>0.001||result[i]<-0.001){
            t3 = t3->insertAfter(result[i],i);
        }
    }
    delete [] result;
    t3->link = NULL;
    return res;
}
#endif