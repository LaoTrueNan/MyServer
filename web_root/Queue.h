#ifndef QUEUE_H
#define QUEUE_H
#include <LinkNode.h>
template <class T>
class Queue{
private:
    //front始终指向附加头结点
    LinkNode<T> *front,*rear;
public:
    Queue(){
       rear = new LinkNode<T>();
       front = rear;
    }
    ~Queue(){
        makeEmpty();
    }
    void makeEmpty(){
        LinkNode<T> *temp = front->next;
        while(temp != NULL){
            front->next = temp->next;
            delete temp;
            temp = front->next;
        }
    }
    bool isEmpty(){
        return front == rear ? true:false;
    }
    bool enqueue(const T d);
    bool dequeue(T &d);
};
//入队
template <class T>
bool Queue<T>::enqueue(const T d){
    LinkNode<T> *newnode,*pre;
    newnode = new LinkNode<T>(d);
    pre = rear;
    rear = newnode;
    pre->next = rear;    
    return true;
};
//出队
template <class T>
bool Queue<T>::dequeue(T &d){
    if(isEmpty()){
        return false;
    }
    LinkNode<T> *current = front->next;
    front->next = current->next;
    d = current->data;
    delete current;
    return true;
};
#endif
