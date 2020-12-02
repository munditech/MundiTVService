Mundi Upnp/DLNA TV Service.
Boot on Start.

Set Command Method:

1. open Mundi Sound Bar Apps method : app name with correct name (examples : 100TV / QPlay / 全民K歌 / 爱奇艺) 繁體/簡體無法自己互換

2. open youtube video by video id : [ytvideo]{videoid}

Non Standard DMR commands : 
 類型 : urn:schemas-upnp-org:service:RenderingControl
 *GetPackages : 
      out parameters : null 
      return values : jason array of packages 

 *GetCommand :
      out parameters : null
      return values : last command from DMC

 *SetCommand :
      out parameters : command string 
      return values : none

