#!/usr/bin/env python

import sys,os
import random
import numpy as np 

initializationTuples = 200020000 # tuples used for prior of beta distribution
N = 20000 + initializationTuples # total number of tuples
pTrainPos = 0.2 # probability that a positive tuple is in training data
pTrainNeg = 0.2 # probability that a negative tuple is in training data
T_p = 0.5 # base tuple truth rate
T = np.matrix([[T_p],[1-T_p]])

N_G = 1 + 0 # 1 group for all sources, and 0 additional ones.
A_G = [] # group accuracies
G_1fp = 0.001 # false positive for group 1
G_1fn = 0.001 # false negative for group 1
G = np.matrix([[1-G_1fn, G_1fp],[G_1fn, 1-G_1fp]])
A_G.append(G)
for k in range(1,N_G):
    G_kfp = 0.4 # false positive for group 1
    G_kfn = 0.4 # false negative for group 1
    G_k = np.matrix([[1-G_kfn, G_kfp],[G_kfn, 1-G_kfp]])
    A_G.append(G_k)

N_S = 1# number of sources.
A_S = [] # source accuracies
for j in range(0,N_S):
    S_1fp = 0.001 # false positive for source 1
    S_1fn = 0.001 # false negative for source 1
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

tuples_file = "data/tuples.tsv"
source_outputs_file = "data/source_tuples.csv"
source_groups_file = "data/source_group.csv"
group_beliefs_file = "data/group_beliefs.csv"

f_tuples = open(tuples_file,'w')
f_source_outputs = open(source_outputs_file,'w')
f_source_groups = open(source_groups_file,'w')
f_group_beliefs = open(group_beliefs_file,'w')

#logfiles, containing latent variables
tuples_latent = "data/latent/tuples.tsv"
group_beliefs_latent = "data/latent/group_belief.tsv"
source_group_beliefs_latent = "data/latent/source_group_beliefs.tsv"
f_tuples_latent = open(tuples_latent,'w')
f_group_beliefs_latent = open(group_beliefs_latent,'w')
f_source_group_beliefs_latent = open(source_group_beliefs_latent,'w')

trueMatrix = np.matrix([[1.0],[0.0]])
falseMatrix = np.matrix([[0.0],[1.0]])

for k in range(0,N_G):
    for j in G[k]:
        f_source_groups.write('{},{}\n'.format(j,k))

for i in range(0,N + initializationTuples):
    val = "null"
    S_T_i = [] # odds for sources for displaying this tuple
    for j in range(0,N_S):
        S_T_i.append(1.0)
    T_i = falseMatrix
    if (random.random() < T.item((0,0))):
        T_i = trueMatrix
    if (T_i == trueMatrix).all():
        f_tuples_latent.write('{}\t{}\n'.format(i,'true'))
        if random.random() < pTrainPos:
            val = "true"
    else:
        f_tuples_latent.write('{}\t{}\n'.format(i,'false'))
        if random.random() < pTrainNeg:
            val = "false"
    f_tuples.write('{}\t{}\n'.format(i,val))
    for k in range(0,N_G):
        G_kp = A_G[k]*T_i
        G_k = falseMatrix
        if random.random() < G_kp.item((0,0)):
            G_k = trueMatrix
        if (G_k == trueMatrix).all():
            f_group_beliefs_latent.write('{}\t{}\t{}\n'.format(k,i,'true'))
            if i < initializationTuples:
                f_group_beliefs.write('{},{},{}\n'.format(k,i,'true'))
        else:
            f_group_beliefs_latent.write('{}\t{}\t{}\n'.format(k,i,'false'))
            if i < initializationTuples:
                f_group_beliefs.write('{},{},{}\n'.format(k,i,'false'))
        for j in G[k]:
            S_jp = A_S[j]*G_k
            S_j = falseMatrix
            if random.random() < S_jp.item((0,0)):
                S_j = trueMatrix
            if (S_j == trueMatrix).all():
                f_source_group_beliefs_latent.write('{}\t{}\t{}\t{}\n'.format(j,k,i,'true'))
                S_T_i[j] *= 300.0 #since 2 is the default weight we've given to the source output factors.
            else:
                f_source_group_beliefs_latent.write('{}\t{}\t{}\t{}\n'.format(j,k,i,'false'))
                S_T_i[j] /= 300.0;
            #S_T_i[j] *= (S_jp.item((0,0)))/(S_jp.item((1,0)))
    for j in range(0,N_S):
        prob = S_T_i[j]/(1 + S_T_i[j])
        if random.random() < prob:
            f_source_outputs.write('{},{},{}\n'.format(j,i,'true'))

f_tuples.close()
f_source_outputs.close() 
f_source_groups.close()
