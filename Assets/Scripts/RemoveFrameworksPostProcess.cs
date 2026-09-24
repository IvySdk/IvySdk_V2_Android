using System.IO;
using UnityEditor;
using UnityEditor.Callbacks;
using UnityEngine;

#if UNITY_IOS
using UnityEditor.iOS.Xcode;
#endif

public class RemoveFrameworksPostProcess
{
    [PostProcessBuild(999)]
    public static void OnPostProcessBuild(BuildTarget target, string pathToBuiltProject)
    {
        if (target != BuildTarget.iOS) return;

#if UNITY_IOS
        string projPath = PBXProject.GetPBXProjectPath(pathToBuiltProject);
        PBXProject proj = new PBXProject();
        proj.ReadFromFile(projPath);

#if UNITY_2019_3_OR_NEWER
        string targetGuid = proj.GetUnityFrameworkTargetGuid();
#else
        string targetGuid = proj.TargetGuidByName("Unity-iPhone");
#endif

        // 从 Xcode 项目 target 中移除指定 Framework
        if (proj.ContainsFramework(targetGuid, "StoreKit.framework"))
        {
            proj.RemoveFrameworkFromProject(targetGuid, "StoreKit.framework");
        }

        if (proj.ContainsFramework(targetGuid, "GameKit.framework"))
        {
            proj.RemoveFrameworkFromProject(targetGuid, "GameKit.framework");
        }

        proj.WriteToFile(projPath);
        Debug.Log("[PostProcessBuild] 已成功移除 StoreKit 与 GameKit Framework。");
#endif
    }
}