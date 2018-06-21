import pandas as pd


df = pd.DataFrame({'from': ['a', 'b', 'a'], 'to': ['b', 'a', 'c'], 'value': [10, 10, 5]})
print(df)
list = []
for i in range(df.shape[1]):
    #print([hash(ord(df.iat[i,0])+ord(df.iat[i,1])), [df.iat[i,0], df.iat[i,1], df.iat[i,2]]])
    list.append([hash(ord(df.iat[i,0])+ord(df.iat[i,1])), df.iat[i,0], df.iat[i,1], df.iat[i,2]])

newdf = pd.DataFrame(list)
uniquedf = newdf.drop_duplicates(subset=(newdf.columns[0]), keep='first', inplace=False)

uniquedf.columns = ['hash','from','to','value']
uniquedf = uniquedf.iloc[:,1:4]

print(uniquedf)
