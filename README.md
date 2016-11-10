
# GroCodePrototype


When you upload this code it will probably not work. Intellij will complain that two modules is missing.
I did refresh gradel project and added a local.properties tex edeting document to the projects root foolder,
with this in side it:

sdk.dir=/Users/johanringstrom/Library/Android/sdk

change it to your path. This is ofcaurse not a good way of dooing it. Better ideas will be appreciated.

Functions with this Erlang kode:
-module(emqtcc_minimal_project).

%% API exports
-export([init/0, subscribe/2, start/0, connect/0, getList/3, addToDB/4, removeFromDB/4, decode/1, getListsOfLists/2]).


%To make this work you have to add this dependencies to your reaber project. 
%{erl_opts, [debug_info]}.
%{deps, [{emqttc, {git, "https://github.com/emqtt/emqttc.git", {ref, %"815ebeca103025bbb5eb8e4b2f6a5f79e1236d4c"}}},
%{mysql, ".*", {git, "https://github.com/mysql-otp/mysql-otp", {tag, "1.2.0"}}}
%]}.
%%====================================================================
%% API functions
%%====================================================================
init()->
    %Connects to hivem online broker
  {ok, C} = emqttc:start_link([{host, "test.mosquitto.org"}, {client_id, <<"GroCood2016">>}, {keepalive, 300}, {connack_timeout, 60}]),
  {ok,PidDbD}=connect(),
  emqttc:subscribe(C, <<"RootGro/#">>, qos1),    %Start subscribing to a topic.
  subscribe(C, PidDbD).  % Start receiveloop to receive messages on the subscribed topic.
  
% Databas connect dunction  
connect()->
    ([{host, "mysql18.citynetwork.se"}, {user, "116955-dw57706"},
                             {password, "GrocodeMaster123"}, {database, "116955-grocode"}]).
    
    %{ok, Pid} = mysql:start_link([{host, "127.0.0.1"}, {user, "root"},
                             % {password, "Ranyrg1324"}, {database, "GroCode"}]).
                              
   
    
   

% Receiveloop to receive messages on the subscribed topic
subscribe(C,PidDbD) ->    
            receive 
                {publish, Topic, Product} -> 
                %Decode Message and puts it in a list with tuples.
                    [{_,BinClientId},{_, BinTodo},{_,[{BinListName, BinItem}]}]  = decode(Product),
                       
                       %Checks wht to do with pattern matching.
                       case [{BinTodo, BinItem}] of
                       
                        [{<<"add">>, BinItem}] ->
                            addToDB(PidDbD,BinListName, BinClientId, BinItem);
                            
                        [{<<"delete">>, BinItem}] ->
                            removeFromDB(PidDbD,BinListName, BinClientId, BinItem);
                            
                        [{<<"getList">>, BinItem}] ->
                            %Puts list int to tuple variables.
                            {ok, ColumnNames, Rows} = getList(PidDbD, BinListName, BinClientId),
                            %Makes the list binary.
                            BinRows = [{<<"items">>,[list_to_binary(R)|| R <- Rows]}],
                            %Creates topic of With RootClient and Client Id.
                            ClientTopic = "RootClient/"++binary_to_list(BinClientId),
                            %Publis a the items found in a json array.
                            emqttc:publish(C, list_to_binary(ClientTopic) , jsx:encode(BinRows));
                            
                        [{<<"getListsOfLists">>, BinItem}] ->
                            %Puts list int to tuple variables.
                            {ok, ColumnNames, Rows} = getListsOfLists(PidDbD, BinClientId),
                            %Makes the list binary.
                            BinRows = [{<<"lists">>,[list_to_binary(R)|| R <- Rows]}],
                            %Creates topic of With RootClient and Client Id.
                            ClientTopic = "RootClient/"++binary_to_list(BinClientId),
                            %Publis a the items found in a json array.
                            emqttc:publish(C, list_to_binary(ClientTopic) , jsx:encode(BinRows));
                            
                        [{<<"createList">>, _}] -> 
                            createList(PidDbD, BinListName, BinClientId)
                     end, 
                     subscribe(C, PidDbD)
                
            end.
                    
 
 createList(PidDbD,ListName, ClientId) ->
    %Insert User and list name to the table List
     mysql:query(PidDbD, "INSERT INTO  Lists (ListName, UserName) VALUES (?, ?)", [ListName, ClientId]).
     
 getListsOfLists(PidDbD, ClientId) ->
    %Takes from the Lists table the listName connected to given clientId.
    {ok, ColumnNames, Rows} = mysql:query(PidDbD, "SELECT ListName FROM Lists WHERE UserName = (?) ", [ClientId]).
 
 getList(PidDbD, ListName, ClientId) ->
    %Takes from the Lists table the listId connected to given list name and clientId.
    {ok, ColumnNames, Rows} = mysql:query(PidDbD, "SELECT ListId FROM Lists WHERE UserName = (?) AND ListName = (?)", [ClientId, ListName]),
    [[ListId]]=Rows,
    %Takes item from the list that corresponde with the listId. 
     mysql:query(PidDbD, "SELECT ItemName FROM Items WHERE ListId = (?)", [ListId]).
    
 
addToDB(PidDbD, ListName, ClientId, Item )->
    %Takes from the Lists table the listId connected to given list name and clientId.
  {ok, ColumnNames, Rows} = mysql:query(PidDbD, "SELECT ListId FROM Lists WHERE UserName = (?) AND ListName = (?)", [ClientId, ListName]),
    [[ListId]]=Rows,
    io:format(">>?? ~p", [ListId]),
    %Puts item to the list that corresponde with the listId.
    ok = mysql:query(PidDbD, "INSERT INTO  Items (ListId, ItemName) VALUES (?, ?)", [ListId, Item]).

removeFromDB(PidDbD, ListName, ClientId, Item ) ->
    %Takes from the Lists table the listId connected to given list name and clientId.
    {ok, ColumnNames, Rows} = mysql:query(PidDbD, "SELECT ListId FROM Lists WHERE UserName = (?) AND ListName = (?)", [ClientId, ListName]),
    [[ListId]]=Rows,
     %Deletes item to the list that corresponde with the listId.
    ok = mysql:query(PidDbD, "DELETE FROM  Items WHERE ListId=(?) AND ItemName=(?)",  [ListId, Item]).

%Takes a message and decode it from jason object to a list of tuples.
decode(Message) ->  
try	
[{<<"clientId">>,ID},
 {<<"request">>,Req},
 {<<"data">>,[{List,Item}]}]
 = jsx:decode(Message)
 catch error:E -> [{<<"client_id">>,1},
 {<<"request">>,<<"ERROR">>},
 {<<"data">>,[{<<"item">>,c}]}]
 end.
        


%If the process sts is spawned the is_pid funktion will return true and return ok and the sts pid. 
%Otherweise it spawns the init function that initializes the serverloop with an empty list. And register the spawned 
%function with the name sts and returns an ok and the sts pid.
start() ->
	case is_pid(whereis(sts)) of
  	true -> {ok, whereis(sts)};
	 _	 -> Pid= spawn(emqtcc_minimal_project, init, []),
			register(sts, Pid),
			{ok, Pid}
	end.
