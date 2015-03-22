import sys,os
import random
import numpy as np 

N = 100000 # total number of tuples
pTrainPos = 0.1 # probability that a positive tuple is in training data
pTrainNeg = 0.001 # probability that a negative tuple is in training data
T_p = 0.01 # base tuple truth rate
T = np.matrix([[T_p],[1-T_p]])

N_G = 1 + 0 # 1 group for all sources, and 0 additional ones.
A_G = [] # group accuracies
for k in range(0,N_G):
    G_1fp = 0.001 # false positive for group 1
    G_1fn = 0.4 # false negative for group 1
    G = np.matrix([[1-G_1fn, G_1fp],[G_1fn, 1-G_1fp]])
    A_G.append(G)

N_S = 10 # number of sources.
A_S = [] # source accuracies
for j in range(0,N_S):
    S_1fp = 0.001 # false positive for source 1
    S_1fn = 0.4 # false negative for source 1
    S = np.matrix([[1-S_1fn, S_1fp],[S_1fn, 1-S_1fp]])
    A_S.append(S)

pGS = 0.2 # probability of a source belonging to a group (other than the all
# containing group)
G = [] # list of group member-lists 
G.append(range(0,N_S)) #first group has all sources as members
for k in range(1,N_G):
    grp = []
    for j in range(0,N_S):
        if random.random() < pGS:
            grp.append(j)
    G.append(grp)

tuples_file = "data/tuples2.tsv"
source_outputs_file = "data/source_tuples2.csv"
source_groups_file = "data/source_group2.csv"

f_tuples = open(tuples_file,'w')
f_source_outputs = open(source_outputs_file,'w')
f_source_groups = open(source_groups_file,'w')

trueMatrix = np.matrix([[1.0],[0.0]])
falseMatrix = np.matrix([[0.0],[1.0]])

for k in range(0,N_G):
    for j in G[k]:
        f_source_groups.write('{},{}\n'.format(j,k))

for i in range(0,N):
    val = "null"
    S_T_i = [] # odds for sources for displaying this tuple
    for j in range(0,N_S):
        S_T_i.append(0)
    T_i = falseMatrix
    if (random.random() < T.item((0,0))):
        T_i = trueMatrix
    if (T_i == trueMatrix).all():
        if random.random() < pTrainPos:
            val = "true"
    else:
        if random.random() < pTrainNeg:
            val = "false"
    f_tuples.write('{}\t{}\n'.format(i,val))
    for k in range(0,N_G):
        G_kp = A_G[k]*T_i
        G_k = falseMatrix
        if random.random() < G_kp.item((0,0)):
            G_k = trueMatrix
        for j in G[k]:
            if S_T_i[j] == 0:
                S_T_i[j] = 1
            S_jp = A_S[j]*G_k
            S_T_i[j] *= (S_jp.item((0,0)))/(S_jp.item((1,0)))
    for j in range(0,N_S):
        prob = S_T_i[j]/(1 + S_T_i[j])
        if random.random() < prob:
            f_source_outputs.write('{},{},{}\n'.format(j,i,'true'))

f_tuples.close()
f_source_outputs.close() 
f_source_groups.close()
