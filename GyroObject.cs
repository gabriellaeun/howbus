﻿using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class GyroObject : MonoBehaviour
{
    private Gyroscope gyro;
    private bool gyroSupported;
    private Quaternion rotFix;

    [SerializeField]
    private Transform wObj;
    private float startY;

    [SerializeField]
    private Transform zObj;
    // Start is called before the first frame update
    void Start()
    {
        gyroSupported = SystemInfo.supportsGyroscope;

        GameObject cam = new GameObject("cam");
        cam.transform.position = transform.position;
        transform.parent = cam.transform;

        if (gyroSupported)
        {
            gyro = Input.gyro;
            gyro.enabled = true;//자이로스코프 켜고 끄기

            cam.transform.rotation = Quaternion.Euler(90f, 180f, 0f);//object의 현재 방향
            rotFix = new Quaternion(0, 0, 1, 0);
        }
    }

    // Update is called once per frame
    void Update()
    {
        if (gyroSupported && startY == 0)
        {
            ResetGyroRotation();
        }

        transform.localRotation = gyro.attitude * rotFix;
    }

    void ResetGyroRotation()
    {
        int x = Screen.width / 2;
        int y = Screen.height / 2;


        Ray ray = Camera.main.ScreenPointToRay(new Vector3(x, y));
        RaycastHit hit;

        if (Physics.Raycast(ray, out hit, 500))
        {
            Vector3 hitPoint = hit.point;
            hitPoint.y = 0;

            float z = Vector3.Distance(Vector3.zero, hitPoint);
            zObj.localPosition = new Vector3(0f, zObj.localPosition.y, Mathf.Clamp(z, 2f, 10f));
        }


        startY = transform.eulerAngles.y;
        wObj.rotation = Quaternion.Euler(0f, startY, 0f);
    }
}
